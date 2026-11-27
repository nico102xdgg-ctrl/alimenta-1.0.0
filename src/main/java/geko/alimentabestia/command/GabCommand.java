package geko.alimentabestia.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import geko.alimentabestia.AlimentaBestia;
import geko.alimentabestia.GameConfig;
import geko.alimentabestia.ModBlocks;
import geko.alimentabestia.ModEntities;
import geko.alimentabestia.ModItems;
import geko.alimentabestia.entity.FocaEntity;
import geko.alimentabestia.game.GameManager;
import geko.alimentabestia.game.SeatLinks;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class GabCommand {
    private static final int PERMISSION_LEVEL = 2;

    @SubscribeEvent
    public void onRegister(RegisterCommandsEvent event) {
        build(event.getDispatcher());
    }

    private static void build(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gab")
                .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                .then(Commands.literal("status").executes(GabCommand::status))
                .then(Commands.literal("start").executes(ctx -> setEnabled(ctx, true))
                        .then(Commands.literal("all").executes(ctx -> setEnabled(ctx, true))))
                .then(Commands.literal("stop").executes(ctx -> setEnabled(ctx, false)))
                .then(Commands.literal("turn").executes(GabCommand::turn))
                .then(Commands.literal("config")
                        .then(Commands.literal("create_seat").executes(GabCommand::createSeat))
                        .then(Commands.literal("spawn_seal").executes(GabCommand::spawnSeal))
                        .then(Commands.literal("link_tool").executes(GabCommand::linkTool))
                        .then(Commands.literal("hunger").executes(GabCommand::showHunger)
                                .then(Commands.argument("segundos", FloatArgumentType.floatArg(5.0f, 600.0f))
                                        .executes(GabCommand::setHunger)))));
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        GameManager manager = GameManager.get();
        boolean enabled = manager.isEnabled();
        int playing = manager.playing();
        ctx.getSource().sendSuccess(() -> Component.literal("Alimenta Bestia: ").withStyle(ChatFormatting.GRAY)
                .append(enabled
                        ? Component.literal("ACTIVADO").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                        : Component.literal("DESACTIVADO").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(Component.literal(enabled ? "  ·  " + playing + " jugando" : "").withStyle(ChatFormatting.DARK_GRAY)), false);
        return enabled ? 1 : 0;
    }

    private static int turn(CommandContext<CommandSourceStack> ctx) {
        GameManager manager = GameManager.get();
        boolean enable = !manager.isEnabled();
        announce(ctx, enable, manager.setEnabled(enable));
        return 1;
    }

    private static int setEnabled(CommandContext<CommandSourceStack> ctx, boolean enable) {
        GameManager manager = GameManager.get();
        if (manager.isEnabled() == enable) {
            ctx.getSource().sendFailure(Component.literal(enable ? "El juego ya estaba activado." : "El juego ya estaba desactivado."));
            return 0;
        }
        announce(ctx, enable, manager.setEnabled(enable));
        return 1;
    }

    private static void announce(CommandContext<CommandSourceStack> ctx, boolean enabled, int started) {
        ctx.getSource().sendSuccess(() -> Component.literal("Alimenta Bestia ").withStyle(ChatFormatting.GRAY)
                .append(enabled
                        ? Component.literal("activado").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                        : Component.literal("desactivado").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(Component.literal(enabled
                        ? " · " + started + (started == 1 ? " jugador sentado entra" : " jugadores sentados entran")
                        : " · todos levantados y las sillas libres").withStyle(ChatFormatting.DARK_GRAY)), true);
    }

    private static int showHunger(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> hungerLine("El hambre tarda"), false);
        return Math.round(GameConfig.hungerEmptySeconds());
    }

    private static int setHunger(CommandContext<CommandSourceStack> ctx) {
        GameConfig.setHungerEmptySeconds(FloatArgumentType.getFloat(ctx, "segundos"));
        ctx.getSource().sendSuccess(() -> hungerLine("Ahora el hambre tarda"), true);
        return Math.round(GameConfig.hungerEmptySeconds());
    }

    private static Component hungerLine(String prefix) {
        return Component.literal(prefix + " ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(seconds(GameConfig.hungerEmptySeconds()) + "s").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .append(Component.literal(" de foca sin alimentar en vaciarse entera").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("  ·  de fabrica " + seconds(GameConfig.HUNGER_EMPTY_SECONDS_DEFAULT)
                        + "s · mas segundos = mas facil · vuelve al de fabrica al reiniciar").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static String seconds(float value) {
        return value == (int) value ? String.valueOf((int) value) : String.valueOf(value);
    }

    private static int createSeat(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        if (!level.getBlockState(pos).isAir()) {
            ctx.getSource().sendFailure(Component.literal("Ahi ya hay un bloque. Ponte donde quieras la silla y vuelve a intentarlo."));
            return 0;
        }
        BlockState state = ModBlocks.SILLA.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, player.getDirection());
        level.setBlockAndUpdate(pos, state);
        ctx.getSource().sendSuccess(() -> Component.literal("Silla puesta en ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(describe(pos)).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" · enlazale una foca con /gab config link_tool").withStyle(ChatFormatting.DARK_GRAY)), true);
        return 1;
    }

    private static int spawnSeal(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        try {
            FocaEntity foca = new FocaEntity(ModEntities.FOCA.get(), level);
            int id = SeatLinks.get(level).takeFocaId();
            foca.setFocaId(id);
            foca.setPersistenceRequired();
            foca.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0f);
            foca.setYHeadRot(player.getYRot());
            foca.setYBodyRot(player.getYRot());
            foca.setAnimState(FocaEntity.AnimState.REPOSO);
            level.addFreshEntity(foca);
            ctx.getSource().sendSuccess(() -> Component.literal("Foca #" + id + " creada").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(" · enlazala a una silla con /gab config link_tool").withStyle(ChatFormatting.DARK_GRAY)), true);
            return 1;
        } catch (Exception e) {
            AlimentaBestia.LOGGER.error("Error al spawnear la foca", e);
            ctx.getSource().sendFailure(Component.literal("Fallo al spawnear la foca: revisa la consola"));
            return 0;
        }
    }

    private static int linkTool(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack tool = new ItemStack(ModItems.LINK_TOOL.get());
        if (!player.getInventory().add(tool)) {
            player.drop(tool, false);
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Vara de enlace").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(" · click derecho en una foca y luego en su silla; agachado en la silla, quita el enlace")
                        .withStyle(ChatFormatting.DARK_GRAY)), false);
        return 1;
    }

    private static String describe(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}
