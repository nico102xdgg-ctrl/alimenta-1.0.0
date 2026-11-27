package geko.alimentabestia.client.hud;

import com.mojang.blaze3d.vertex.VertexConsumer;
import geko.alimentabestia.ModSounds;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public final class CelebrationEffect {
    private static final long PEAK_AT_MS = 360L;
    private static final long HOLD_UNTIL_MS = 600L;
    private static final long END_AT_MS = 1000L;
    private static final int COLOUR_RGB = 2415104;
    private static final float MAX_ALPHA = 0.36862746f;
    private static final float CLEAR_RADIUS = 0.29f;
    private static final float SOLID_RADIUS = 0.92f;
    private static final float SOUND_VOLUME = 1.0f;
    private static final int COLUMNS = 48;
    private static final int ROWS = 27;
    private static final float[] vertexAlpha = new float[(COLUMNS + 1) * (ROWS + 1)];

    private static boolean active;
    private static long startedAt;

    private CelebrationEffect() {
    }

    public static void trigger() {
        active = true;
        startedAt = Util.getMillis();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.PLAYER_PASS.get(), 1.0f, SOUND_VOLUME));
    }

    public static void reset() {
        active = false;
    }

    public static void render(GuiGraphics graphics) {
        if (!active) {
            return;
        }
        long elapsed = Util.getMillis() - startedAt;
        if (elapsed >= END_AT_MS) {
            active = false;
            return;
        }
        if (Minecraft.getInstance().options.hideGui) {
            return;
        }
        float envelope = envelope(elapsed);
        if (envelope <= 0.0f) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        computeVertexAlpha(width, height);

        VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.gui());
        Matrix4f matrix = graphics.pose().last().pose();
        for (int row = 0; row < ROWS; row++) {
            float y0 = (float) (height * row) / ROWS;
            float y1 = (float) (height * (row + 1)) / ROWS;
            for (int col = 0; col < COLUMNS; col++) {
                float a00 = alphaAt(col, row);
                float a01 = alphaAt(col, row + 1);
                float a11 = alphaAt(col + 1, row + 1);
                float a10 = alphaAt(col + 1, row);
                if (a00 + a01 + a11 + a10 <= 0.0f) {
                    continue;
                }
                float x0 = (float) (width * col) / COLUMNS;
                float x1 = (float) (width * (col + 1)) / COLUMNS;
                buffer.vertex(matrix, x0, y0, 0.0f).color(colour(a00, envelope)).endVertex();
                buffer.vertex(matrix, x0, y1, 0.0f).color(colour(a01, envelope)).endVertex();
                buffer.vertex(matrix, x1, y1, 0.0f).color(colour(a11, envelope)).endVertex();
                buffer.vertex(matrix, x1, y0, 0.0f).color(colour(a10, envelope)).endVertex();
            }
        }
        graphics.flush();
    }

    private static float envelope(long elapsed) {
        if (elapsed < PEAK_AT_MS) {
            return smoothstep(elapsed / 360.0f);
        }
        if (elapsed < HOLD_UNTIL_MS) {
            return 1.0f;
        }
        return 1.0f - smoothstep((elapsed - HOLD_UNTIL_MS) / 400.0f);
    }

    private static void computeVertexAlpha(int width, int height) {
        float halfW = width / 2.0f;
        float halfH = height / 2.0f;
        float maxRadius = Mth.sqrt(halfW * halfW + halfH * halfH);
        for (int row = 0; row <= ROWS; row++) {
            float dy = (float) (height * row) / ROWS - halfH;
            for (int col = 0; col <= COLUMNS; col++) {
                float dx = (float) (width * col) / COLUMNS - halfW;
                float dist = Mth.sqrt(dx * dx + dy * dy) / maxRadius;
                vertexAlpha[row * (COLUMNS + 1) + col] =
                        MAX_ALPHA * smoothstep((dist - CLEAR_RADIUS) / (SOLID_RADIUS - CLEAR_RADIUS));
            }
        }
    }

    private static float alphaAt(int col, int row) {
        return vertexAlpha[row * (COLUMNS + 1) + col];
    }

    private static int colour(float alpha, float envelope) {
        int a = Math.round(alpha * envelope * 255.0f);
        return (a << 24) | COLOUR_RGB;
    }

    private static float smoothstep(float t) {
        float x = Mth.clamp(t, 0.0f, 1.0f);
        return x * x * (3.0f - 2.0f * x);
    }
}
