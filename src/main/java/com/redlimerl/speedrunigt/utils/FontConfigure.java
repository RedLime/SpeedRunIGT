package com.redlimerl.speedrunigt.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redlimerl.speedrunigt.SpeedRunIGT;

public class FontConfigure {

    public float size;
    public float oversample;
    public float[] shift;
    public String skip;

    public static FontConfigure create() {
        return new FontConfigure(11, 6, new float[] { 0, 0 }, "");
    }

    public static FontConfigure fromJson(String json) {
        JsonObject configure = JsonParser.parseString(json).getAsJsonObject();
        FontConfigure fontConfigure = create();
        if (configure.has("size")) fontConfigure.size = configure.get("size").getAsFloat();
        if (configure.has("oversample")) fontConfigure.oversample = configure.get("oversample").getAsFloat();
        if (configure.has("shift") && configure.get("shift").isJsonArray()) {
            JsonArray shifts = configure.get("shift").getAsJsonArray();
            if (shifts.size() >= 1) {
                fontConfigure.shift[0] = shifts.get(0).getAsFloat();
            }
            if (shifts.size() >= 2) {
                fontConfigure.shift[1] = shifts.get(1).getAsFloat();
            }
        }
        if (configure.has("size")) fontConfigure.size = configure.get("size").getAsFloat();
        if (configure.has("skip")) fontConfigure.skip = configure.get("skip").getAsString();
        return fontConfigure;
    }

    public FontConfigure(float size, float oversample, float[] shift, String skip) {
        this.size = size;
        this.oversample = oversample;
        this.shift = shift;
        this.skip = skip;
    }

    @Override
    public String toString() {
        return SpeedRunIGT.PRETTY_GSON.toJson(this);
    }
}
