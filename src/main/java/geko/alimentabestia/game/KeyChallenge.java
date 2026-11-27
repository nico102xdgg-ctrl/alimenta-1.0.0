package geko.alimentabestia.game;

import geko.alimentabestia.GameConfig;

public final class KeyChallenge {
    private final char letter;
    private final int cell;
    private final float x;
    private final float y;
    private int presses;
    private int ticksLeft = GameConfig.KEY_LIFETIME_TICKS;
    private int lastPressTick = -2;

    KeyChallenge(char letter, int cell, float x, float y) {
        this.letter = letter;
        this.cell = cell;
        this.x = x;
        this.y = y;
    }

    public char letter() {
        return letter;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    int cell() {
        return cell;
    }

    
    boolean tick() {
        return --ticksLeft <= 0;
    }

    boolean press(int tick) {
        if (tick - lastPressTick < GameConfig.KEY_PRESS_DEBOUNCE_TICKS) {
            return false;
        }
        lastPressTick = tick;
        presses++;
        return true;
    }

    boolean isComplete() {
        return presses >= GameConfig.KEY_PRESSES_REQUIRED;
    }
}
