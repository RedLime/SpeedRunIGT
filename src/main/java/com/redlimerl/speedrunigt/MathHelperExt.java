package com.redlimerl.speedrunigt;

public class MathHelperExt {
    public static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        } else {
            return Math.min(value, max);
        }
    }
}
