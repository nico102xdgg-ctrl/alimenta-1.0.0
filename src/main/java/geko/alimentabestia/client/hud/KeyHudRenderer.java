package geko.alimentabestia.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import geko.alimentabestia.AlimentaBestia;
import geko.alimentabestia.client.ClientMinigameState;
import geko.alimentabestia.net.KeyView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class KeyHudRenderer {
    private static final int ALPHABET_SIZE = 26;
    private static final int KEY_SIZE = 28;
    private static final int TEXTURE_SIZE = 60;
    private static final ResourceLocation[] SPRITE_IDLE = new ResourceLocation[ALPHABET_SIZE];
    private static final ResourceLocation[] SPRITE_PRESSED = new ResourceLocation[ALPHABET_SIZE];

    static {
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            char letter = (char) ('a' + i);
            SPRITE_IDLE[i] = AlimentaBestia.id("textures/gui/sprites/tecla/idle/" + letter + ".png");
            SPRITE_PRESSED[i] = AlimentaBestia.id("textures/gui/sprites/tecla/press/" + letter + ".png");
        }
    }

    private KeyHudRenderer() {
    }

    public static void render(GuiGraphics graphics) {
        if (!ClientMinigameState.areKeysActive() || Minecraft.getInstance().options.hideGui) {
            return;
        }
        List<KeyView> keys = ClientMinigameState.keys();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        RenderSystem.enableBlend();
        for (KeyView key : keys) {
            int index = key.letter() - 'a';
            if (index < 0 || index >= ALPHABET_SIZE) {
                continue;
            }
            ResourceLocation sprite = ClientMinigameState.isFlashing(key.letter())
                    ? SPRITE_PRESSED[index] : SPRITE_IDLE[index];
            int x = Math.round(key.x() * width) - KEY_SIZE / 2;
            int y = Math.round(key.y() * height) - KEY_SIZE / 2;
            graphics.blit(sprite, x, y, KEY_SIZE, KEY_SIZE, 0.0f, 0.0f, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        RenderSystem.disableBlend();
    }
}
