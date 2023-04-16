package com.redlimerl.speedrunigt.therun;

import com.redlimerl.speedrunigt.timer.InGameTimer;

import java.util.LinkedHashMap;
import java.util.function.Function;

public class TheRunCategory {

    private final String gameName;
    private final Function<InGameTimer, String> categoryNameFunction;
    private final Function<InGameTimer, LinkedHashMap<String, String>> splitNameMap;
    private final String completedSplitName;

    private TheRunCategory(String gameName, Function<InGameTimer, String> categoryNameFunction, Function<InGameTimer, LinkedHashMap<String, String>> splitNameMap, String completedSplitName) {
        this.gameName = gameName;
        this.categoryNameFunction = categoryNameFunction;
        this.splitNameMap = splitNameMap;
        this.completedSplitName = completedSplitName;
    }

    public String getGameName() {
        return gameName;
    }

    public String getCategoryName(InGameTimer timer) {
        return categoryNameFunction.apply(timer);
    }

    public LinkedHashMap<String, String> getSplitNameMap(InGameTimer timer) {
        return splitNameMap.apply(timer);
    }

    public String getCompletedSplitName() {
        return completedSplitName;
    }

    public static class Builder {
        private String gameName;
        private Function<InGameTimer, LinkedHashMap<String, String>> splitNameMap;
        private String completedSplitName;
        private Function<InGameTimer, String> categoryNameFunction;

        public Builder setGameName(String gameName) {
            this.gameName = gameName;
            return this;
        }

        public Builder setCategoryName(String categoryName) {
            this.categoryNameFunction = timer -> categoryName;
            return this;
        }

        public Builder setCategoryNameFunction(Function<InGameTimer, String> categoryNameFunction) {
            this.categoryNameFunction = categoryNameFunction;
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
            if (this.gameName == null || this.categoryNameFunction == null || this.splitNameMap == null || this.completedSplitName == null) throw new IllegalArgumentException();
            return new TheRunCategory(this.gameName, this.categoryNameFunction, this.splitNameMap, this.completedSplitName);
        }
    }
}
