package geko.alimentabestia.item;

import geko.alimentabestia.block.SillaBlock;
import geko.alimentabestia.entity.FocaEntity;
import geko.alimentabestia.game.SeatLinks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LinkToolItem extends Item {
    private static final Map<UUID, UUID> selection = new HashMap<>();

    public LinkToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (!(target instanceof FocaEntity foca)) {
            return InteractionResult.PASS;
        }
        selection.put(player.getUUID(), foca.getUUID());
        BlockPos seat = SeatLinks.get(serverPlayer.serverLevel()).seatOf(foca.getUUID());
        serverPlayer.displayClientMessage(
                Component.literal("Foca #" + foca.getFocaId() + " elegida").withStyle(ChatFormatting.AQUA)
                        .append(seat == null
                                ? Component.literal(" · ahora click derecho en una silla").withStyle(ChatFormatting.GRAY)
                                : Component.literal(" · ya estaba en la silla " + describe(seat)).withStyle(ChatFormatting.GRAY)),
                false);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos();
        ServerLevel level = serverPlayer.serverLevel();
        if (!(level.getBlockState(pos).getBlock() instanceof SillaBlock)) {
            return InteractionResult.PASS;
        }
        SeatLinks links = SeatLinks.get(level);

        if (player.isShiftKeyDown()) {
            boolean removed = links.unlink(pos);
            serverPlayer.displayClientMessage(removed
                    ? Component.literal("Enlace quitado de la silla " + describe(pos)).withStyle(ChatFormatting.YELLOW)
                    : Component.literal("Esa silla no tenia foca enlazada.").withStyle(ChatFormatting.GRAY), false);
            return InteractionResult.CONSUME;
        }

        UUID selected = selection.get(player.getUUID());
        if (selected == null) {
            serverPlayer.displayClientMessage(
                    Component.literal("Primero click derecho en la foca que quieras enlazar.").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }
        Entity entity = level.getEntity(selected);
        if (!(entity instanceof FocaEntity foca)) {
            selection.remove(player.getUUID());
            serverPlayer.displayClientMessage(
                    Component.literal("Esa foca ya no existe. Elige otra.").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }
        links.link(pos, selected);
        serverPlayer.displayClientMessage(
                Component.literal("Silla " + describe(pos)).withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(" enlazada con la foca #" + foca.getFocaId()).withStyle(ChatFormatting.WHITE)),
                false);
        return InteractionResult.CONSUME;
    }

    private static String describe(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}
