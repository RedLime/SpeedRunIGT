package com.redlimerl.speedrunigt.option;

import net.minecraft.util.Identifier;

public abstract class OptionArgument<T> {
    private final Identifier key;
    private final T defaultValue;

    public OptionArgument(Identifier key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public Identifier getKey() {
        return key;
    }

    public abstract T valueFromString(String string);

    public abstract String valueToString(T value);
}
