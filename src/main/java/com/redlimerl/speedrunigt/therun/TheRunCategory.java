package com.redlimerl.speedrunigt.therun;

import com.redlimerl.speedrunigt.timer.InGameTimer;

import java.util.LinkedHashMap;
import java.util.function.Function;

public class TheRunCategory {
    private final String gameName;
    private final String categoryName;
    private final Function<InGameTimer, LinkedHashMap<String, String>> splitNameMap;
    private final String completedSplitName;

    private TheRunCategory(String gameName, String categoryName, Function<InGameTimer, LinkedHashMap<String, String>> splitNameMap, String completedSplitName) {
        this.gameName = gameName;
        this.categoryName = categoryName;
        this.splitNameMap = splitNameMap;
        this.completedSplitName = completedSplitName;
    }

    public String getGameName() {
        return this.gameName;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public LinkedHashMap<String, String> getSplitNameMap(InGameTimer timer) {
        return splitNameMap.apply(timer);
    }

    public String getCompletedSplitName() {
        return this.completedSplitName;
    }

    public static class Builder {
        private String gameName;
        private String categoryName;
        private Function<InGameTimer, LinkedHashMap<String, String>> splitNameMap;
        private String completedSplitName;

        public Builder setGameName(String gameName) {
            this.gameName = gameName;
            return this;
        }

        public Builder setCategoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public Builder setSplitNameMap(Function<InGameTimer, LinkedHashMap<String, String>> splitNameMap) {
            this.splitNameMap = splitNameMap;
            return this;
        }

        public Builder setCompletedSplitName(String completedSplitName) {
            this.completedSplitName = completedSplitName;
            return this;
        }

        public TheRunCategory build() {
            if (this.gameName == null || this.categoryName == null || this.splitNameMap == null || this.completedSplitName == null) throw new IllegalArgumentException();
            return new TheRunCategory(this.gameName, this.categoryName, this.splitNameMap, this.completedSplitName);
        }
    }
}
