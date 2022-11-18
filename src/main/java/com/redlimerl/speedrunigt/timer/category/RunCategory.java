package com.redlimerl.speedrunigt.timer.category;

import com.google.gson.JsonArray;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.therun.TheRunCategory;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class RunCategory {
    private final String id;
    private final String categoryUrl;
    private final String translateKey;
    private final boolean autoStart;
    private final boolean canSegment;
    private final boolean customUrl;
    private final boolean hideCategory;
    private final Function<InGameTimer, Boolean> retimeFunction;
    private final @Nullable String conditionFileName;
    private final @Nullable JsonArray conditionJson;
    private final @Nullable TheRunCategory theRunCategory;
    private static final LinkedHashMap<String, RunCategory> CATEGORIES = new LinkedHashMap<>();

    public RunCategory(String id, String categoryUrl) {
        this(id, categoryUrl, "speedrunigt.option.timer_category." + id.toLowerCase(Locale.ROOT));
    }

    public RunCategory(String id, String categoryUrl, String translateKey) {
        this(id, categoryUrl, translateKey, null, null);
    }

    public RunCategory(String id, String categoryUrl, String translateKey, @Nullable String conditionFileName, @Nullable JsonArray conditionJson) {
        this(id, categoryUrl, translateKey, conditionFileName, conditionJson, true, false, false, false, (value) -> false, null);
    }

    public RunCategory(
            String id,
            String categoryUrl,
            String translateKey,
            @Nullable String conditionFileName,
            @Nullable JsonArray conditionJson,
            boolean autoStart,
            boolean canSegment,
            boolean customUrl,
            boolean hideCategory,
            Function<InGameTimer, Boolean> retimeFunction,
            @Nullable TheRunCategory theRunCategory
    ) {
        this.id = id;
        this.categoryUrl = categoryUrl;
        this.translateKey = translateKey;
        this.conditionFileName = conditionFileName;
        this.conditionJson = conditionJson;
        this.autoStart = autoStart;
        this.canSegment = canSegment;
        this.customUrl = customUrl;
        this.hideCategory = hideCategory;
        this.retimeFunction = retimeFunction;
        this.theRunCategory = theRunCategory;
    }

    public static Map<String, RunCategory> getCategories() {
        return CATEGORIES;
    }

    public static RunCategory getCategory(String id) {
        if (!CATEGORIES.containsKey(id)) {
            SpeedRunIGT.error("Couldn't find category with ID \"" + id + "\"!");
            return RunCategories.ERROR_CATEGORY;
        }
        return CATEGORIES.get(id);
    }

    public static void registerCategory(RunCategory category) {
        //if (SpeedRunIGT.isInitialized()) return;
        for (String id : CATEGORIES.keySet()) {
            if (id.equalsIgnoreCase(category.getID())) {
                throw new IllegalArgumentException("ID \"" + id + "\" is an already registered RunCategory ID.");
            }
        }

        CATEGORIES.put(category.getID(), category);
    }

    public String getID() {
        return this.id;
    }

    public String getLeaderboardUrl() {
        return (this.customUrl ? "" : "https://www.speedrun.com/") + this.categoryUrl;
    }

    public boolean canSegment() {
        return this.canSegment;
    }

    public boolean isAutoStart() {
        return this.autoStart;
    }

    public boolean isNeedAutoRetime(InGameTimer timer) {
        return this.retimeFunction.apply(timer);
    }

    public boolean isHideCategory() {
        return this.hideCategory;
    }

    public TranslatableText getText() {
        return new TranslatableText(this.translateKey);
    }

    public @Nullable JsonArray getConditionJson() {
        return this.conditionJson;
    }

    public @Nullable String getConditionFileName() {
        return this.conditionFileName;
    }

    public @Nullable TheRunCategory getTheRunCategory() {
        return this.theRunCategory;
    }
}
