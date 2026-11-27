package geko.alimentabestia.client;

import geko.alimentabestia.net.KeyView;
import geko.alimentabestia.net.MinigameStatePayload;
import net.minecraft.client.KeyMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClientMinigameState {
    private static final int PRESS_FLASH_TICKS = 3;
    private static final int ALPHABET_SIZE = 26;

    private static final List<KeyView> keys = new ArrayList<>();
    private static final int[] pressFlash = new int[ALPHABET_SIZE];
    private static boolean movementLocked;
    private static boolean keysActive;

    private ClientMinigameState() {
    }

    public static void accept(MinigameStatePayload payload) {
        if (payload.keysActive() && !keysActive) {
            KeyMapping.releaseAll();
        }
        movementLocked = payload.movementLocked();
        keysActive = payload.keysActive();
        keys.clear();
        keys.addAll(payload.keys());
    }

    public static void reset() {
        movementLocked = false;
        keysActive = false;
        keys.clear();
        Arrays.fill(pressFlash, 0);
    }

    public static void tick() {
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            if (pressFlash[i] > 0) {
                pressFlash[i]--;
            }
        }
    }

    public static boolean isMovementLocked() {
        return movementLocked;
    }

    public static boolean areKeysActive() {
        return keysActive;
    }

    public static List<KeyView> keys() {
        return keys;
    }

    public static void flash(char letter) {
        pressFlash[letter - 'a'] = PRESS_FLASH_TICKS;
    }

    public static boolean isFlashing(char letter) {
        return pressFlash[letter - 'a'] > 0;
    }
}
