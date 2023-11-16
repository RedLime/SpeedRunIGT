package com.redlimerl.speedrunigt.utils;

import java.util.function.Supplier;

public class MonadicStringBuilder {
    private final StringBuilder stringBuilder;

    public MonadicStringBuilder() {
        this.stringBuilder = new StringBuilder();
    }

    public MonadicStringBuilder append(String text) {
        this.stringBuilder.append(text);
        return this;
    }

    public MonadicStringBuilder appendIf(Supplier<Boolean> condition, String text) {
        if (condition.get()) {
            this.append(text);
        }
        return this;
    }

    public String toString() {
        return this.stringBuilder.toString();
    }
}
