package geko.alimentabestia.client;

import geko.alimentabestia.ModBlocks;
import geko.alimentabestia.ModEntities;
import geko.alimentabestia.client.hud.BossBarHud;
import geko.alimentabestia.client.hud.CelebrationEffect;
import geko.alimentabestia.client.hud.KeyHudRenderer;
import geko.alimentabestia.client.render.CeboRenderer;
import geko.alimentabestia.client.render.FocaRenderer;
import geko.alimentabestia.client.render.SillaBlockRenderer;
import geko.alimentabestia.entity.AsientoEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.player.Input;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class ClientEvents {
    private ClientEvents() {
    }

    
    @Mod.EventBusSubscriber(modid = "geko_alimentabestia", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlocks.SILLA_BLOCK_ENTITY.get(), ctx -> new SillaBlockRenderer());
            event.registerEntityRenderer(ModEntities.CEBO.get(), CeboRenderer::new);
            event.registerEntityRenderer(ModEntities.FOCA.get(), FocaRenderer::new);
            event.registerEntityRenderer(ModEntities.ASIENTO.get(), NoopRenderer::new);
        }

        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("alimentabestia_hud", (gui, graphics, partialTick, width, height) -> {
                CelebrationEffect.render(graphics);
                KeyHudRenderer.render(graphics);
            });
        }
    }

    
    @Mod.EventBusSubscriber(modid = "geko_alimentabestia", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeBus {
        
        private static final int MOUNT_MESSAGE_TICKS = 60;
        private static int hideOverlayTicks;
        private static boolean wasSitting;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            Minecraft mc = Minecraft.getInstance();
            if (event.phase == TickEvent.Phase.START) {
                KeyInputHandler.reserveLetters(mc);
                return;
            }
            ClientMinigameState.tick();
            KeyInputHandler.tick(mc);

            boolean sitting = mc.player != null && mc.player.getVehicle() instanceof AsientoEntity;
            if (sitting && !wasSitting) {
                hideOverlayTicks = MOUNT_MESSAGE_TICKS;
            }
            wasSitting = sitting;
            if (hideOverlayTicks > 0) {
                hideOverlayTicks--;
            }
        }

        
        @SubscribeEvent
        public static void onMovementInput(MovementInputUpdateEvent event) {
            if (!ClientMinigameState.isMovementLocked()) {
                return;
            }
            Input input = event.getInput();
            input.forwardImpulse = 0.0f;
            input.leftImpulse = 0.0f;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            input.jumping = false;
            input.shiftKeyDown = false;
        }

        
        @SubscribeEvent
        public static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
            if (BossBarHud.isCustom(event.getBossEvent())) {
                BossBarHud.render(event.getGuiGraphics(), event.getBossEvent());
                event.setCanceled(true);
                event.setIncrement(0);
            }
        }

        
        @SubscribeEvent
        public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {
            if (hideOverlayTicks > 0 && event.getOverlay() == VanillaGuiOverlay.RECORD_OVERLAY.type()) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ClientMinigameState.reset();
            CelebrationEffect.reset();
            hideOverlayTicks = 0;
            wasSitting = false;
        }
    }
}
