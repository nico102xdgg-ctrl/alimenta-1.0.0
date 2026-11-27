package geko.alimentabestia;

public final class GameConfig {
    public static final int TICKS_PER_SECOND = 20;
    public static final int SEAL_INTERVAL_TICKS = 400;
    public static final int SEAL_BEG_TICKS = 200;
    public static final int RECONNECT_GRACE_TICKS = 1200;

    public static final float HUNGER_MAX = 100.0f;
    public static final float HUNGER_EMPTY_SECONDS_DEFAULT = 45.0f;
    public static final float HUNGER_EMPTY_SECONDS_MIN = 5.0f;
    public static final float HUNGER_EMPTY_SECONDS_MAX = 600.0f;
    private static float hungerEmptySeconds = HUNGER_EMPTY_SECONDS_DEFAULT;

    public static final float FISH_MAX = 1000.0f;
    public static final float FISH_PER_PRESS = 18.19f;

    public static final int KEY_PRESSES_REQUIRED = 10;
    public static final int KEY_LIFETIME_TICKS = 200;
    public static final int KEY_RESPAWN_DELAY_TICKS = 10;
    public static final int KEY_PRESS_DEBOUNCE_TICKS = 2;
    public static final int KEY_SLOTS_START = 1;
    public static final int KEY_SLOTS_MAX = 5;
    public static final int KEY_SLOT_RAMP_TICKS = 600;

    public static final float KEY_AREA_MIN_X = 0.1f;
    public static final float KEY_AREA_MAX_X = 0.9f;
    public static final float KEY_AREA_MIN_Y = 0.3f;
    public static final float KEY_AREA_MAX_Y = 0.76f;
    public static final int KEY_GRID_COLS = 4;
    public static final int KEY_GRID_ROWS = 3;
    public static final int KEY_GRID_CELLS = 12;
    public static final float KEY_CELL_JITTER = 0.6f;

    public static final double MOVEMENT_LOCK_TOLERANCE = 0.35d;
    public static final int SEAL_EAT_ANIMATION_TICKS = 100;
    public static final int SEAL_DEATH_ANIMATION_TICKS = 160;

    private GameConfig() {
    }

    public static float hungerEmptySeconds() {
        return hungerEmptySeconds;
    }

    public static void setHungerEmptySeconds(float seconds) {
        hungerEmptySeconds = Math.min(HUNGER_EMPTY_SECONDS_MAX, Math.max(HUNGER_EMPTY_SECONDS_MIN, seconds));
    }

    public static float hungerDrainPerTick() {
        return HUNGER_MAX / (hungerEmptySeconds * 20.0f);
    }
}
