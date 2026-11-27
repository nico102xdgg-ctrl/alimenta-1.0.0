package geko.alimentabestia.game;

import geko.alimentabestia.GameConfig;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public final class KeyBoard {
    private static final int ALPHABET_SIZE = 26;

    private final KeyChallenge[] slots = new KeyChallenge[GameConfig.KEY_SLOTS_MAX];
    private final RandomSource random;
    private int nextTandaDelay;
    private int previousTandaLetters;
    private boolean dirty;

    KeyBoard(RandomSource random) {
        this.random = random;
    }

    private static int tandaSize(int elapsedTicks) {
        int extra = elapsedTicks / GameConfig.KEY_SLOT_RAMP_TICKS;
        return Math.min(GameConfig.KEY_SLOTS_MAX, GameConfig.KEY_SLOTS_START + extra);
    }

    void tick(int elapsedTicks) {
        if (!isEmpty()) {
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] != null && slots[i].tick()) {
                    retire(i);
                }
            }
            return;
        }
        if (--nextTandaDelay <= 0) {
            spawnTanda(tandaSize(elapsedTicks));
        }
    }

    
    float press(char letter, int tick) {
        for (int i = 0; i < slots.length; i++) {
            KeyChallenge key = slots[i];
            if (key == null || key.letter() != letter) {
                continue;
            }
            if (!key.press(tick)) {
                return 0.0f;
            }
            dirty = true;
            if (key.isComplete()) {
                retire(i);
                return GameConfig.FISH_PER_PRESS;
            }
        }
        return 0.0f;
    }

    void clear() {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                slots[i] = null;
                dirty = true;
            }
        }
        nextTandaDelay = 0;
    }

    List<KeyChallenge> active() {
        List<KeyChallenge> list = new ArrayList<>(GameConfig.KEY_SLOTS_MAX);
        for (KeyChallenge key : slots) {
            if (key != null) {
                list.add(key);
            }
        }
        return list;
    }

    boolean isDirty() {
        return dirty;
    }

    void clearDirty() {
        dirty = false;
    }

    private boolean isEmpty() {
        for (KeyChallenge key : slots) {
            if (key != null) {
                return false;
            }
        }
        return true;
    }

    private void retire(int index) {
        slots[index] = null;
        dirty = true;
        if (isEmpty()) {
            nextTandaDelay = GameConfig.KEY_RESPAWN_DELAY_TICKS;
        }
    }

    private void spawnTanda(int size) {
        for (int i = 0; i < size; i++) {
            spawn(i);
        }
        int letters = 0;
        for (KeyChallenge key : slots) {
            if (key != null) {
                letters |= 1 << (key.letter() - 'a');
            }
        }
        previousTandaLetters = letters;
    }

    private void spawn(int index) {
        boolean[] usedLetters = new boolean[ALPHABET_SIZE];
        boolean[] usedCells = new boolean[GameConfig.KEY_GRID_CELLS];
        int freeLetters = ALPHABET_SIZE;
        int freeCells = GameConfig.KEY_GRID_CELLS;

        for (int l = 0; l < ALPHABET_SIZE; l++) {
            if ((previousTandaLetters & (1 << l)) != 0) {
                usedLetters[l] = true;
                freeLetters--;
            }
        }
        for (KeyChallenge key : slots) {
            if (key == null) {
                continue;
            }
            int l = key.letter() - 'a';
            if (!usedLetters[l]) {
                usedLetters[l] = true;
                freeLetters--;
            }
            if (!usedCells[key.cell()]) {
                usedCells[key.cell()] = true;
                freeCells--;
            }
        }
        if (freeLetters <= 0 || freeCells <= 0) {
            return;
        }

        int letter = pickFree(usedLetters, freeLetters);
        int cell = pickFree(usedCells, freeCells);
        slots[index] = new KeyChallenge((char) ('a' + letter), cell, cellX(cell), cellY(cell));
        dirty = true;
    }

    private int pickFree(boolean[] used, int freeCount) {
        int n = random.nextInt(freeCount);
        for (int i = 0; i < used.length; i++) {
            if (!used[i] && n-- == 0) {
                return i;
            }
        }
        return 0;
    }

    private float cellX(int cell) {
        int col = cell % GameConfig.KEY_GRID_COLS;
        float cellWidth = (GameConfig.KEY_AREA_MAX_X - GameConfig.KEY_AREA_MIN_X) / GameConfig.KEY_GRID_COLS;
        return GameConfig.KEY_AREA_MIN_X + cellWidth * (col + jitter());
    }

    private float cellY(int cell) {
        int row = cell / GameConfig.KEY_GRID_COLS;
        float cellHeight = (GameConfig.KEY_AREA_MAX_Y - GameConfig.KEY_AREA_MIN_Y) / GameConfig.KEY_GRID_ROWS;
        return GameConfig.KEY_AREA_MIN_Y + cellHeight * (row + jitter());
    }

    private float jitter() {
        float margin = (1.0f - GameConfig.KEY_CELL_JITTER) / 2.0f;
        return margin + GameConfig.KEY_CELL_JITTER * random.nextFloat();
    }
}
