package com.redlimerl.speedrunigt;

public class SpeedRunIGTConfig {
    static SpeedRunIGTConfig config;

    public static SpeedRunIGTConfig getConfig() {
        return config;
    }

    public final Boolean isDedicated;
    SpeedRunIGTConfig(Boolean isDedicated) {
        this.isDedicated = isDedicated;
    }
}
