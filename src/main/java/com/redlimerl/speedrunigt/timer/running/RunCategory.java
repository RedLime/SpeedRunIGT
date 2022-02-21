package com.redlimerl.speedrunigt.timer.running;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.minecraft.text.TranslatableText;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class RunCategory {

    private static final LinkedHashMap<String, RunCategory> CATEGORIES = new LinkedHashMap<>();
    public static Map<String, RunCategory> getCategories() {
        return CATEGORIES;
    }
    public static RunCategory getCategory(String id) {
        if (!CATEGORIES.containsKey(id)) {
            SpeedRunIGT.error("Not found category with ID \"" + id + "\"!");
            return RunCategories.ANY;
        }
        return CATEGORIES.get(id);
    }

    public static void registerCategory(RunCategory category) {
        if (SpeedRunIGT.isInitialized()) return;
        for (String id : CATEGORIES.keySet()) {
            if (id.equalsIgnoreCase(category.getID())) {
                throw new IllegalArgumentException("This is an already registered RunCategory ID.");
            }
        }

        CATEGORIES.put(category.getID(), category);
    }


    private final String id;
    private final String srcCategory;

    public RunCategory(String id, String srcCategory) {
        this.id = id;
        this.srcCategory = srcCategory;
    }

    public String getID() {
        return id;
    }

    public String getSRCLeaderboardUrl() {
        return "https://www.speedrun.com/" + srcCategory;
    }

    public TranslatableText getText() {
        return new TranslatableText("speedrunigt.option.timer_category." + id.toLowerCase(Locale.ROOT));
    }
}
