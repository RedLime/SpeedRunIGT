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
        return this.context;
    }

    public int getCode() {
        return this.code;
    }

    public static RunType fromBoolean(boolean isSetSeed) {
        return isSetSeed ? SET_SEED : RANDOM_SEED;
    }

    public static RunType fromInt(int code) {
        for (RunType type : RunType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return RANDOM_SEED;
    }
}
