package com.redlimerl.speedrunigt.timer.running;

public enum RunType {
    RANDOM_SEED("random_seed", 0), SET_SEED("set_seed", 1), OLD_WORLD("old_world", 2);

    private final String context;
    private final int code;

    RunType(String s, int code) {
        this.context = s;
        this.code = code;
    }

    public String getContext() {
        return context;
    }

    public int getCode() {
        return code;
    }

    public static RunType fromBoolean(boolean isSetSeed) {
        return isSetSeed ? SET_SEED : RANDOM_SEED;
    }

    public static RunType fromInt(int code) {
        if (code == 0) return RANDOM_SEED;
        if (code == 1) return SET_SEED;
        return OLD_WORLD;
    }
}
