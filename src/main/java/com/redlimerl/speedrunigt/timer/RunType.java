package com.redlimerl.speedrunigt.timer;

public enum RunType {
    RSG, SSG, FSG;

    public static RunType getRunType(boolean isSetSeed, boolean isFiltered) {
        return !isSetSeed ? RSG : isFiltered ? FSG : SSG;
    }
}
