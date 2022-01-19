package com.redlimerl.speedrunigt.utils;

import java.util.OptionalLong;

public class DataUtils {
    public static OptionalLong tryParseLong(String string) {
        try {
            return OptionalLong.of(Long.parseLong(string));
        } catch (NumberFormatException var2) {
            return OptionalLong.empty();
        }
    }
}
