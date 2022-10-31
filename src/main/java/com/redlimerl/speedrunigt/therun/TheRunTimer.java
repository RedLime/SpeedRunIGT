package com.redlimerl.speedrunigt.therun;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.logs.TimerTimeline;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.collect.Lists;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class TheRunTimer {

    private final InGameTimer timer;

    public TheRunTimer(InGameTimer timer) {
        this.timer = timer;
    }

    private JsonObject timelineToJsonObject(String title, Long igt) {
        JsonObject splitData = new JsonObject();
        splitData.addProperty("name", title);
        if (igt != null) {
            splitData.addProperty("splitTime", igt);
        } else {
            splitData.add("splitTime", JsonNull.INSTANCE);
        }
        splitData.add("pbSplitTime", JsonNull.INSTANCE);
        splitData.add("bestPossible", JsonNull.INSTANCE);

        JsonArray comparisons = new JsonArray();

        JsonObject target1 = new JsonObject();
        target1.addProperty("name", "Personal Best");
        target1.add("time", JsonNull.INSTANCE);
        comparisons.add(target1);

        JsonObject target2 = new JsonObject();
        target2.addProperty("name", "Best Segments");
        target2.add("time", JsonNull.INSTANCE);
        comparisons.add(target2);

        JsonObject target3 = new JsonObject();
        target3.addProperty("name", "Average Segments");
        target3.add("time", JsonNull.INSTANCE);
        comparisons.add(target3);

        splitData.add("comparisons", comparisons);
        return splitData;
    }

    public JsonObject convertJson() {
        @Nullable TheRunCategory category = timer.getCategory().getTheRunCategory();
        if (category == null) throw new NullPointerException();

        List<TimerTimeline> timelines = timer.getTimelines();
        LinkedHashMap<String, String> splits = category.getSplitNameMap(timer);
        Collection<String> remainSplits = Lists.newArrayList();
        remainSplits.addAll(splits.values());

        List<String> completedSplits = Lists.newArrayList();
        JsonArray allSplits = new JsonArray();

        for (TimerTimeline timeline : timelines) {
            if (!splits.containsKey(timeline.getName())) {
                continue;
            }

            String categoryTitle = splits.get(timeline.getName());
            remainSplits.remove(categoryTitle);
            completedSplits.add(categoryTitle);

            allSplits.add(timelineToJsonObject(categoryTitle, timeline.getIGT()));
        }

        if (!timer.isCompleted()) {
            for (String remainSplit : remainSplits) {
                allSplits.add(timelineToJsonObject(remainSplit, null));
            }
        } else {
            completedSplits.add(category.getCompletedSplitName());
        }

        allSplits.add(timelineToJsonObject(category.getCompletedSplitName(), timer.isCompleted() ? timer.getInGameTime() : null));


        JsonObject jsonObject = new JsonObject();

        JsonObject metaData = new JsonObject();
        metaData.addProperty("game", category.getGameName());
        metaData.addProperty("category", category.getCategoryName());
        metaData.addProperty("platform", "");
        metaData.addProperty("region", "");
        metaData.addProperty("emulator", false);
        metaData.add("variables", new JsonObject());
        jsonObject.add("metadata", metaData);

        jsonObject.addProperty("currentTime", timer.getInGameTime(false));


        jsonObject.addProperty("currentSplitName", completedSplits.size() > 0 ? completedSplits.get(completedSplits.size() - 1) : "");
        jsonObject.addProperty("currentSplitIndex", completedSplits.size());
        jsonObject.addProperty("timingMethod", 1);
        jsonObject.addProperty("currentDuration", timer.getRealTimeAttack());
        jsonObject.addProperty("startTime", ("/Date(" + Instant.ofEpochMilli(timer.getStartTime()).atZone(ZoneOffset.UTC).toInstant().toEpochMilli() + ")/").trim());
        jsonObject.addProperty("endTime", ("/Date(" + (timer.isCompleted() ? Instant.ofEpochMilli(timer.getEndTime()).atZone(ZoneOffset.UTC).toInstant().toEpochMilli() : "0") + ")/").trim());
        jsonObject.addProperty("uploadKey", TheRunKeyHelper.UPLOAD_KEY);
        jsonObject.add("runData", allSplits);

        return jsonObject;
    }
}
