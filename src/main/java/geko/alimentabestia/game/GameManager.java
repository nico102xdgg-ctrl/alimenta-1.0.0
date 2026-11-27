package geko.alimentabestia.game;

import geko.alimentabestia.entity.AsientoEntity;
import geko.alimentabestia.block.SillaBlock;
import geko.alimentabestia.net.MinigameStatePayload;
import geko.alimentabestia.net.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GameManager {
    private static final GameManager INSTANCE = new GameManager();

    private final Map<UUID, PlayerSession> sessions = new HashMap<>();
    private final Map<BlockPos, UUID> claimedSeats = new HashMap<>();
    private MinecraftServer server;
    private boolean enabled;

    private GameManager() {
    }

    public static GameManager get() {
        return INSTANCE;
    }

    public static void registerEvents() {
        MinecraftForge.EVENT_BUS.register(new GameEvents());
    }

    void onServerStarted(MinecraftServer server) {
        this.server = server;
    }

    public boolean isEnabled() {
        return enabled;
    }

    
    public int setEnabled(boolean flag) {
        if (enabled == flag || server == null) {
            enabled = flag;
            return 0;
        }
        enabled = flag;
        if (!enabled) {
            stopEveryone();
            return 0;
        }
        int started = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            BlockPos seat = seatOf(player);
            if (seat != null && startSession(player, seat)) {
                started++;
            }
        }
        return started;
    }

    private void stopEveryone() {
        for (Map.Entry<UUID, PlayerSession> entry : sessions.entrySet()) {
            ServerPlayer player = playerOf(entry.getKey());
            entry.getValue().dispose(player);
            if (player != null) {
                player.stopRiding();
                ModNetwork.sendTo(player, MinigameStatePayload.INACTIVE);
            }
        }
        sessions.clear();
        claimedSeats.clear();
    }

    public boolean trySit(ServerPlayer player, BlockPos pos) {
        if (player.isPassenger()) {
            return false;
        }
        if (enabled) {
            UUID claimed = claimedSeats.get(pos);
            if (claimed != null && !claimed.equals(player.getUUID())) {
                return false;
            }
            PlayerSession existing = sessions.get(player.getUUID());
            if (existing != null && !existing.seat().equals(pos)) {
                return false;
            }
        }
        if (!AsientoEntity.sit(player.level(), pos, player)) {
            return false;
        }
        if (enabled && !sessions.containsKey(player.getUUID())) {
            startSession(player, pos);
        }
        return true;
    }

    private BlockPos seatOf(ServerPlayer player) {
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof AsientoEntity asiento)) {
            return null;
        }
        BlockPos pos = asiento.blockPosition();
        return player.level().getBlockState(pos).getBlock() instanceof SillaBlock ? pos : null;
    }

    private boolean startSession(ServerPlayer player, BlockPos pos) {
        if (claimedSeats.containsKey(pos)) {
            return false;
        }
        PlayerSession session = new PlayerSession(player, pos);
        sessions.put(player.getUUID(), session);
        claimedSeats.put(pos.immutable(), player.getUUID());
        session.start(player);
        return true;
    }

    void tick() {
        if (!enabled || sessions.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, PlayerSession> entry : sessions.entrySet()) {
            entry.getValue().tick(playerOf(entry.getKey()));
        }
    }

    void onJoin(ServerPlayer player) {
        PlayerSession session = sessions.get(player.getUUID());
        if (enabled && session != null) {
            session.onReconnect(player);
        }
    }

    void onDisconnect(ServerPlayer player) {
        PlayerSession session = sessions.get(player.getUUID());
        if (session != null) {
            session.onDisconnect(player);
        }
    }

    private ServerPlayer playerOf(UUID id) {
        return server == null ? null : server.getPlayerList().getPlayer(id);
    }

    void shutdown() {
        if (enabled) {
            stopEveryone();
        }
        sessions.clear();
        claimedSeats.clear();
        enabled = false;
        server = null;
    }

    public void notifyAdmins(Component message) {
        if (server == null) {
            return;
        }
        Component full = Component.literal("[Alimenta Bestia] ").withStyle(ChatFormatting.DARK_AQUA).append(message);
        server.sendSystemMessage(full);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.hasPermissions(2)) {
                player.sendSystemMessage(full);
            }
        }
    }

    public void onKeyPress(ServerPlayer player, char letter) {
        PlayerSession session = sessions.get(player.getUUID());
        if (session != null) {
            session.onKeyPress(player, letter);
        }
    }

    public boolean toggleCast(ServerPlayer player) {
        PlayerSession session = sessions.get(player.getUUID());
        return session != null && session.toggleCast(player);
    }

    public boolean feedSeal(ServerPlayer player) {
        PlayerSession session = sessions.get(player.getUUID());
        return session != null && session.feedSeal(player);
    }

    public int playing() {
        return sessions.size();
    }
}
