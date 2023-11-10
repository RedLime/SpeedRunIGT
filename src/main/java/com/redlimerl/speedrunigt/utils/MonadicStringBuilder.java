package com.redlimerl.speedrunigt.utils;

import java.util.function.Supplier;

public class MonadicStringBuilder {
    StringBuilder stringBuilder;

    public MonadicStringBuilder() {
        this(new StringBuilder());
    }

    MonadicStringBuilder(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }

    public MonadicStringBuilder append(String text) {
        return new MonadicStringBuilder(this.stringBuilder.append(text));
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
