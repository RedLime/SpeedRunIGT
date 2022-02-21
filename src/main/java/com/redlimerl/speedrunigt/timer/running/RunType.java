package com.redlimerl.speedrunigt.timer.running;

public enum RunType {
    RANDOM_SEED("Random Seed"), SET_SEED("Set Seed"), FILTERED_SEED("Filtered Seed"), SAVED_WORLD("Saved World");

    private final String context;

    RunType(String s) {
        this.context = s;
    }

    public String getContext() {
        return context;
    }

    public static RunType getRunType(boolean isSetSeed, boolean isFiltered) {
        return !isSetSeed ? RANDOM_SEED : isFiltered ? FILTERED_SEED : SET_SEED;
    }
}
