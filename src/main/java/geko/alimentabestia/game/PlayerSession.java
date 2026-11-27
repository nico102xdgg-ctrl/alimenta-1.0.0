package geko.alimentabestia.game;

import geko.alimentabestia.GameConfig;
import geko.alimentabestia.ModItems;
import geko.alimentabestia.ModSounds;
import geko.alimentabestia.block.SillaBlock;
import geko.alimentabestia.entity.AsientoEntity;
import geko.alimentabestia.entity.CeboEntity;
import geko.alimentabestia.entity.FocaEntity;
import geko.alimentabestia.entity.FocaEntity.AnimState;
import geko.alimentabestia.item.CanaItem;
import geko.alimentabestia.net.KeyView;
import geko.alimentabestia.net.MinigameStatePayload;
import geko.alimentabestia.net.ModNetwork;
import geko.alimentabestia.net.SealFedPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PlayerSession {
    private enum Phase {SEAL_AWAY, SEAL_PRESENT}

    private static final long NO_ROD_ANIM = Long.MAX_VALUE;
    private static final float CAST_POWER = 0.6f;
    private static final float CAST_PITCH = 20.0f;

    private final ServerLevel level;
    private final BlockPos seat;
    private final KeyBoard keys;
    private Phase phase = Phase.SEAL_AWAY;
    private int sealCountdown = GameConfig.SEAL_INTERVAL_TICKS;
    private int sealBegTicksLeft;
    private float hunger = GameConfig.HUNGER_MAX;
    private float fishProgress;
    private int sealsFed;
    private int elapsedTicks;
    private boolean hookLanded;
    private CeboEntity bait;
    private long rodAnimId = NO_ROD_ANIM;
    private boolean eliminated;
    private boolean dying;
    private int deathAnimTicksLeft;
    private int surfaceAnimTicksLeft;
    private int offlineTicks = -1;
    private boolean graceWarned;
    private final ServerBossEvent hungerBar;
    private final ServerBossEvent fishBar;
    private MinigameStatePayload lastSent;
    private boolean stateDirty = true;

    PlayerSession(ServerPlayer player, BlockPos seat) {
        this.level = player.serverLevel();
        this.seat = seat.immutable();
        this.keys = new KeyBoard(player.getRandom());
        this.hungerBar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        this.fishBar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
        attachBars(player);
        refreshBars();
    }

    void start(ServerPlayer player) {
        attachBars(player);
        refreshBars();
        updateFocaAnim(AnimState.NADANDO);
        stateDirty = true;
        syncIfChanged(player);
    }

    BlockPos seat() {
        return seat;
    }

    void tick(ServerPlayer player) {
        if (player == null) {
            if (!tickOffline()) {
                return;
            }
        } else {
            offlineTicks = -1;
            attachBars(player);
            keepSeated(player);
        }
        if (eliminated) {
            return;
        }
        elapsedTicks++;
        tickSealCycle(player);
        tickSurfaceAnimation();
        if (player != null) {
            updateFishingState(player);
            if (areKeysActive(player)) {
                keys.tick(elapsedTicks);
            }
        }
        refreshBars();
        if (player != null) {
            syncIfChanged(player);
        }
    }

    private boolean tickOffline() {
        if (offlineTicks < GameConfig.RECONNECT_GRACE_TICKS) {
            offlineTicks++;
            return false;
        }
        if (!graceWarned) {
            graceWarned = true;
            GameManager.get().notifyAdmins(
                    Component.literal("Se acabo el margen de reconexion:").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(" la partida de la silla " + describeSeat() + " sigue corriendo sin el.")
                                    .withStyle(ChatFormatting.GRAY)));
        }
        return true;
    }

    private void tickSealCycle(ServerPlayer player) {
        if (dying) {
            if (--deathAnimTicksLeft <= 0) {
                eliminate(player);
            }
            return;
        }
        if (phase == Phase.SEAL_AWAY) {
            if (--sealCountdown <= 0) {
                phase = Phase.SEAL_PRESENT;
                onSealArrived(player);
            }
            return;
        }
        hunger -= GameConfig.hungerDrainPerTick();
        if (hunger <= 0.0f) {
            hunger = 0.0f;
            beginDeathAnimation();
            return;
        }
        if (--sealBegTicksLeft <= 0) {
            onSealLeaves(player);
        }
    }

    private void onSealArrived(ServerPlayer player) {
        sealBegTicksLeft = GameConfig.SEAL_BEG_TICKS;
        beginSurfaceAnimation(AnimState.PEDIR, GameConfig.SEAL_BEG_TICKS);
        if (player != null) {
            player.playNotifySound(ModSounds.ALERT.get(), SoundSource.MASTER, 1.0f, 1.0f);
        }
    }

    private static void stopAlert(ServerPlayer player) {
        if (player != null) {
            player.connection.send(new ClientboundStopSoundPacket(ModSounds.ALERT.get().getLocation(), SoundSource.MASTER));
        }
    }

    private void onSealLeaves(ServerPlayer player) {
        phase = Phase.SEAL_AWAY;
        sealCountdown = GameConfig.SEAL_INTERVAL_TICKS;
        sealBegTicksLeft = 0;
        surfaceAnimTicksLeft = 0;
        updateFocaAnim(AnimState.NADANDO);
        stopAlert(player);
    }

    private void beginDeathAnimation() {
        dying = true;
        deathAnimTicksLeft = GameConfig.SEAL_DEATH_ANIMATION_TICKS;
        surfaceAnimTicksLeft = 0;
        updateFocaAnim(AnimState.MURIENDO);
    }

    private void eliminate(ServerPlayer player) {
        eliminated = true;
        dying = false;
        int seconds = elapsedTicks / GameConfig.TICKS_PER_SECOND;
        hungerBar.removeAllPlayers();
        fishBar.removeAllPlayers();
        keys.clear();
        releaseBait(player);
        stopAlert(player);
        updateFocaAnim(AnimState.REPOSO);
        if (player != null) {
            player.sendSystemMessage(Component.literal("¡Has perdido!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                    .append(Component.literal(" La foca se quedo sin comida.").withStyle(ChatFormatting.RED)));
            player.sendSystemMessage(Component.literal("Aguantaste " + seconds + "s y la alimentaste " + sealsFed
                    + (sealsFed == 1 ? " vez" : " veces") + ".").withStyle(ChatFormatting.GRAY));
        }
        stateDirty = true;
        if (player != null) {
            syncIfChanged(player);
        }
        GameManager.get().notifyAdmins(
                Component.literal("Eliminado en la silla " + describeSeat() + ":").withStyle(ChatFormatting.RED)
                        .append(Component.literal(" " + seconds + "s · " + sealsFed + " focas").withStyle(ChatFormatting.GRAY)));
    }

    private void beginSurfaceAnimation(AnimState state, int ticks) {
        surfaceAnimTicksLeft = ticks;
        updateFocaAnim(state);
    }

    private void tickSurfaceAnimation() {
        if (surfaceAnimTicksLeft > 0 && --surfaceAnimTicksLeft <= 0) {
            updateFocaAnim(AnimState.NADANDO);
        }
    }

    private FocaEntity foca() {
        UUID id = SeatLinks.get(level).focaAt(seat);
        if (id == null) {
            return null;
        }
        Entity entity = level.getEntity(id);
        if (entity instanceof FocaEntity foca && !foca.isRemoved()) {
            return foca;
        }
        return null;
    }

    private void updateFocaAnim(AnimState state) {
        FocaEntity foca = foca();
        if (foca != null) {
            foca.setAnimState(state);
        }
    }

    void onKeyPress(ServerPlayer player, char letter) {
        if (!areKeysActive(player)) {
            return;
        }
        float fish = keys.press(letter, elapsedTicks);
        if (fish <= 0.0f) {
            return;
        }
        fishProgress += fish;
        stateDirty = true;
        if (fishProgress >= GameConfig.FISH_MAX) {
            fishProgress = 0.0f;
            giveFish(player);
            releaseBait(player);
        }
        refreshBars();
    }

    private void giveFish(ServerPlayer player) {
        ItemStack fish = new ItemStack(ModItems.PESCADO.get());
        if (!player.getInventory().add(fish)) {
            player.drop(fish, false);
        }
    }

    boolean feedSeal(ServerPlayer player) {
        if (eliminated || phase != Phase.SEAL_PRESENT) {
            return false;
        }
        phase = Phase.SEAL_AWAY;
        sealCountdown = 500;
        sealBegTicksLeft = 0;
        sealsFed++;
        stopAlert(player);
        beginSurfaceAnimation(AnimState.COMIENDO, GameConfig.SEAL_EAT_ANIMATION_TICKS);
        ModNetwork.sendTo(player, SealFedPayload.INSTANCE);
        refreshBars();
        return true;
    }

    boolean toggleCast(ServerPlayer player) {
        if (eliminated || rodStack(player).isEmpty()) {
            return false;
        }
        if (bait != null && !bait.isRemoved()) {
            releaseBait(player);
        } else {
            castBait(player);
        }
        return true;
    }

    private void castBait(ServerPlayer player) {
        releaseBait(player);
        keys.clear();
        float yaw = player.getYRot() * 0.017453292f;
        double pitch = Math.toRadians(CAST_PITCH);
        double horizontal = Math.cos(pitch);
        CeboEntity cebo = new CeboEntity(level, player);
        cebo.shoot(-Mth.sin(yaw) * horizontal, Math.sin(pitch), Mth.cos(yaw) * horizontal, CAST_POWER, 0.0f);
        level.addFreshEntity(cebo);
        bait = cebo;
    }

    private void updateFishingState(ServerPlayer player) {
        if (bait != null && rodStack(player).isEmpty()) {
            releaseBait(player);
            return;
        }
        if (bait == null || bait.isRemoved()) {
            if (hookLanded) {
                hookLanded = false;
                playRodAnimation(player, CanaItem.ANIM_IDLE);
            }
            bait = null;
            return;
        }
        if (!hookLanded && bait.isFloating()) {
            hookLanded = true;
            playRodAnimation(player, CanaItem.ANIM_PESCAR);
        }
    }

    private void playRodAnimation(ServerPlayer player, String animation) {
        ItemStack rod = rodStack(player);
        if (!rod.isEmpty()) {
            rodAnimId = GeoItem.getOrAssignId(rod, player.serverLevel());
        }
        if (rodAnimId == NO_ROD_ANIM) {
            return;
        }
        ModItems.CANA.get().triggerAnim(player, rodAnimId, CanaItem.CONTROLLER, animation);
    }

    private void releaseBait(ServerPlayer player) {
        if (bait != null) {
            bait.discard();
            bait = null;
        }
        if (hookLanded) {
            hookLanded = false;
            if (player != null) {
                playRodAnimation(player, CanaItem.ANIM_IDLE);
            }
        }
    }

    private static ItemStack rodStack(ServerPlayer player) {
        if (player.getMainHandItem().getItem() instanceof CanaItem) {
            return player.getMainHandItem();
        }
        return player.getOffhandItem().getItem() instanceof CanaItem ? player.getOffhandItem() : ItemStack.EMPTY;
    }

    private boolean areKeysActive(ServerPlayer player) {
        return !eliminated && bait != null && !bait.isRemoved() && !rodStack(player).isEmpty();
    }

    boolean isMovementLocked() {
        return true;
    }

    private void keepSeated(ServerPlayer player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof AsientoEntity asiento && asiento.blockPosition().equals(seat)) {
            return;
        }
        if (!(level.getBlockState(seat).getBlock() instanceof SillaBlock)) {
            return;
        }
        if (player.isPassenger()) {
            player.stopRiding();
        }
        AsientoEntity.sit(level, seat, player);
    }

    private String describeSeat() {
        return seat.getX() + " " + seat.getY() + " " + seat.getZ();
    }

    void onDisconnect(ServerPlayer player) {
        offlineTicks = 0;
        graceWarned = false;
        releaseBait(null);
        hungerBar.removeAllPlayers();
        fishBar.removeAllPlayers();
        int graceSeconds = GameConfig.RECONNECT_GRACE_TICKS / GameConfig.TICKS_PER_SECOND;
        String name = player == null ? "Un jugador" : player.getName().getString();
        GameManager.get().notifyAdmins(
                Component.literal(name + " se ha desconectado.").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(" Su partida (silla " + describeSeat() + ") queda congelada " + graceSeconds + "s.")
                                .withStyle(ChatFormatting.GRAY)));
    }

    void onReconnect(ServerPlayer player) {
        offlineTicks = -1;
        graceWarned = false;
        attachBars(player);
        refreshBars();
        keepSeated(player);
        stateDirty = true;
        syncIfChanged(player);
        GameManager.get().notifyAdmins(
                Component.literal(player.getName().getString() + " ha vuelto").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(" · silla " + describeSeat()).withStyle(ChatFormatting.GRAY)));
    }

    private void attachBars(ServerPlayer player) {
        if (eliminated) {
            return;
        }
        if (hungerBar.getPlayers().contains(player)) {
            return;
        }
        hungerBar.removeAllPlayers();
        fishBar.removeAllPlayers();
        hungerBar.addPlayer(player);
        fishBar.addPlayer(player);
    }

    private void refreshBars() {
        hungerBar.setProgress(Mth.clamp(hunger / GameConfig.HUNGER_MAX, 0.0f, 1.0f));
        fishBar.setProgress(Mth.clamp(fishProgress / GameConfig.FISH_MAX, 0.0f, 1.0f));
    }

    private void syncIfChanged(ServerPlayer player) {
        boolean active = areKeysActive(player);
        if (!stateDirty && !keys.isDirty() && lastSent != null
                && lastSent.keysActive() == active && lastSent.movementLocked() == isMovementLocked()) {
            return;
        }
        List<KeyView> views;
        if (active) {
            List<KeyChallenge> current = keys.active();
            views = new ArrayList<>(current.size());
            for (KeyChallenge key : current) {
                views.add(new KeyView(key.letter(), key.x(), key.y()));
            }
        } else {
            views = List.of();
        }
        lastSent = new MinigameStatePayload(isMovementLocked(), active, views);
        stateDirty = false;
        keys.clearDirty();
        ModNetwork.sendTo(player, lastSent);
    }

    void dispose(ServerPlayer player) {
        hungerBar.removeAllPlayers();
        fishBar.removeAllPlayers();
        keys.clear();
        releaseBait(player);
        dying = false;
        surfaceAnimTicksLeft = 0;
        updateFocaAnim(AnimState.REPOSO);
        stopAlert(player);
    }
}
