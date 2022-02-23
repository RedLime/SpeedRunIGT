package com.redlimerl.speedrunigt.version;


public class ColorMixer {
    public static int getAlpha(int argb) {
        return argb >>> 24;
    }

    public static int getRed(int argb) {
        return argb >> 16 & 255;
    }

    public static int getGreen(int argb) {
        return argb >> 8 & 255;
    }

    public static int getBlue(int argb) {
        return argb & 255;
    }

    public static int getArgb(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    public static int mixColor(int first, int second) {
        return getArgb(getAlpha(first) * getAlpha(second) / 255, getRed(first) * getRed(second) / 255, getGreen(first) * getGreen(second) / 255, getBlue(first) * getBlue(second) / 255);
    }
}
