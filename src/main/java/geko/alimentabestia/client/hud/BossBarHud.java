package geko.alimentabestia.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import geko.alimentabestia.AlimentaBestia;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public final class BossBarHud {
    private static final int BAR_WIDTH = 228;
    private static final int BAR_HEIGHT = 43;
    private static final int PERRO_WIDTH = 34;
    private static final int PERRO_HEIGHT = 44;
    private static final int PERRO_OVERLAP = 43;
    private static final int PERRO_X_OFFSET = 18;
    private static final int BAR_BOTTOM_OFFSET = 100;

    private static final int COMIDA_BAR_WIDTH = 228;
    private static final int COMIDA_BAR_HEIGHT = 15;
    private static final int PEZ_WIDTH = 40;
    private static final int PEZ_HEIGHT = 24;
    private static final int PEZ_OVERLAP = 18;
    private static final int PEZ_X_OFFSET = -25;
    private static final int COMIDA_BAR_GAP = 12;
    private static final ResourceLocation HAMBRE_BG = tex("focahambre_background");
    private static final ResourceLocation HAMBRE_PROGRESS = tex("focahambre_1");
    private static final ResourceLocation HAMBRE_PERRO = tex("focahambre_perro");
    private static final ResourceLocation COMIDA_BG = tex("focacomida_background");
    private static final ResourceLocation COMIDA_PROGRESS = tex("focacomida_1");
    private static final ResourceLocation COMIDA_PEZ = tex("focacomida_pez");

    private BossBarHud() {
    }

    private static ResourceLocation tex(String name) {
        return AlimentaBestia.id("textures/gui/sprites/boss_bar/" + name + ".png");
    }

    public static boolean isCustom(BossEvent event) {
        return event.getColor() == BossEvent.BossBarColor.RED || event.getColor() == BossEvent.BossBarColor.BLUE;
    }

    public static void render(GuiGraphics g, BossEvent event) {
        if (event.getColor() == BossEvent.BossBarColor.RED) {
            renderHunger(g, event);
        } else if (event.getColor() == BossEvent.BossBarColor.BLUE) {
            renderFood(g, event);
        }
    }

    private static void renderHunger(GuiGraphics g, BossEvent event) {
        int x = g.guiWidth() / 2 - 114;
        int y = g.guiHeight() - BAR_BOTTOM_OFFSET;
        RenderSystem.enableBlend();
        g.blit(HAMBRE_BG, x, y, 0.0f, 0.0f, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        int fill = Math.round(228.0f * Mth.clamp(event.getProgress(), 0.0f, 1.0f));
        if (fill > 0) {
            g.enableScissor(x, y, x + fill, y + BAR_HEIGHT);
            g.blit(HAMBRE_PROGRESS, x, y, 0.0f, 0.0f, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
            g.disableScissor();
        }
        g.blit(HAMBRE_PERRO, x + fill - PERRO_WIDTH + PERRO_X_OFFSET, y - PERRO_HEIGHT + PERRO_OVERLAP,
                0.0f, 0.0f, PERRO_WIDTH, PERRO_HEIGHT, PERRO_WIDTH, PERRO_HEIGHT);
        RenderSystem.disableBlend();
    }

    private static void renderFood(GuiGraphics g, BossEvent event) {
        int x = g.guiWidth() / 2 - 114;
        int y = g.guiHeight() - BAR_BOTTOM_OFFSET + BAR_HEIGHT + COMIDA_BAR_GAP;
        RenderSystem.enableBlend();
        g.blit(COMIDA_BG, x, y, COMIDA_BAR_WIDTH, COMIDA_BAR_HEIGHT, 0.0f, 0.0f, 139, 9, 139, 9);
        int fill = Math.round(228.0f * Mth.clamp(event.getProgress(), 0.0f, 1.0f));
        if (fill > 0) {
            g.enableScissor(x, y, x + fill, y + COMIDA_BAR_HEIGHT);
            g.blit(COMIDA_PROGRESS, x, y, COMIDA_BAR_WIDTH, COMIDA_BAR_HEIGHT, 0.0f, 0.0f, 139, 9, 139, 9);
            g.disableScissor();
        }
        g.blit(COMIDA_PEZ, x + COMIDA_BAR_WIDTH + PEZ_X_OFFSET, y - PEZ_HEIGHT + PEZ_OVERLAP,
                PEZ_WIDTH, PEZ_HEIGHT, 0.0f, 0.0f, 23, 14, 23, 14);
        RenderSystem.disableBlend();
    }
}
