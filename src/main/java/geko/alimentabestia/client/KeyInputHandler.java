package geko.alimentabestia.client;

import com.mojang.blaze3d.platform.InputConstants;
import geko.alimentabestia.ModSounds;
import geko.alimentabestia.net.KeyPressPayload;
import geko.alimentabestia.net.KeyView;
import geko.alimentabestia.net.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

import java.util.Arrays;

public final class KeyInputHandler {
    private static final int ALPHABET_SIZE = 26;
    private static final boolean[] held = new boolean[ALPHABET_SIZE];
    private static final boolean[] onScreen = new boolean[ALPHABET_SIZE];

    private KeyInputHandler() {
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.screen != null || !ClientMinigameState.areKeysActive()) {
            Arrays.fill(held, false);
            return;
        }
        Arrays.fill(onScreen, false);
        for (KeyView key : ClientMinigameState.keys()) {
            int index = key.letter() - 'a';
            if (index >= 0 && index < ALPHABET_SIZE) {
                onScreen[index] = true;
            }
        }
        long window = mc.getWindow().getWindow();
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            boolean down = InputConstants.isKeyDown(window, 65 + i);
            boolean justPressed = down && !held[i];
            held[i] = down;
            if (justPressed && onScreen[i]) {
                char letter = (char) ('a' + i);
                ClientMinigameState.flash(letter);
                mc.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.TECLA.get(), 1.0f));
                ModNetwork.CHANNEL.sendToServer(new KeyPressPayload(letter));
            }
        }
    }

    
    public static void reserveLetters(Minecraft mc) {
        if (!ClientMinigameState.areKeysActive() || mc.screen != null) {
            return;
        }
        for (net.minecraft.client.KeyMapping mapping : mc.options.keyMappings) {
            InputConstants.Key key = mapping.getKey();
            if (key.getType() == InputConstants.Type.KEYSYM && key.getValue() >= 65 && key.getValue() <= 90) {
                mapping.setDown(false);
                while (mapping.consumeClick()) {
                }
            }
        }
    }
}
