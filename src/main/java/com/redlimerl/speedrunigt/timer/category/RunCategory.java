package com.redlimerl.speedrunigt.timer.category;

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
            return RunCategories.ERROR_CATEGORY;
        }
        return CATEGORIES.get(id);
    }

    public static void registerCategory(RunCategory category) {
        if (SpeedRunIGT.isInitialized()) return;
        for (String id : CATEGORIES.keySet()) {
            if (id.equalsIgnoreCase(category.getID())) {
                throw new IllegalArgumentException("ID \"" + id + "\" is an already registered RunCategory ID.");
            }
        }

        CATEGORIES.put(category.getID(), category);
    }


    private final String id;
    private final String srcCategory;
    private final String translateKey;

    public RunCategory(String id, String srcCategory) {
        this(id, srcCategory, "speedrunigt.option.timer_category." + id.toLowerCase(Locale.ROOT));
    }

    public RunCategory(String id, String srcCategory, String translateKey) {
        this.id = id;
        this.srcCategory = srcCategory;
        this.translateKey = translateKey;
    }

    public String getID() {
        return id;
    }

    public String getSRCLeaderboardUrl() {
        return "https://www.speedrun.com/" + srcCategory;
    }

    public TranslatableText getText() {
        return new TranslatableText(translateKey);
    }
}
