package com.redlimerl.speedrunigt.therun;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.logs.TimerTimeline;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

public class TheRunTimer {

    public enum PacketType {
        PLAYING, RESUME, PAUSE, RESET, COMPLETE
    }

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

    public JsonObject convertJson(PacketType packetType) {
        @Nullable TheRunCategory category = timer.getCategory().getTheRunCategory();
        if (category == null) throw new NullPointerException();

        List<TimerTimeline> timelines = timer.getTimelines();
        LinkedHashMap<String, String> splits = category.getSplitNameMap(timer);
        if (splits == null) return null;

        Collection<String> remainSplits = Lists.newArrayList();
        remainSplits.addAll(splits.values());

        List<String> completedSplits = Lists.newArrayList();
        JsonArray allSplits = new JsonArray();
        long latestIgtPoint = 0;

        for (TimerTimeline timeline : timelines) {
            if (!splits.containsKey(timeline.getName())) {
                continue;
            }

            String categoryTitle = splits.get(timeline.getName());
            remainSplits.remove(categoryTitle);
            completedSplits.add(categoryTitle);

            allSplits.add(timelineToJsonObject(categoryTitle, timeline.getIGT()));
            if (timeline.getIGT() > latestIgtPoint) latestIgtPoint = timeline.getIGT();
        }

        if (!timer.isCompleted()) {
            for (String remainSplit : remainSplits) {
                allSplits.add(timelineToJsonObject(remainSplit, null));
            }
        } else {
            completedSplits.add(category.getCompletedSplitName());
            latestIgtPoint = timer.getInGameTime(false);
        }

        allSplits.add(timelineToJsonObject(category.getCompletedSplitName(), timer.isCompleted() ? timer.getInGameTime() : null));


        JsonObject jsonObject = new JsonObject();

        JsonObject metaData = new JsonObject();
        metaData.addProperty("game", category.getGameName());
        metaData.addProperty("category", category.getCategoryName(timer));
        metaData.addProperty("platform", "");
        metaData.addProperty("region", "");
        metaData.addProperty("emulator", false);
        metaData.add("variables", new JsonObject());
        jsonObject.add("metadata", metaData);

        jsonObject.addProperty("currentTime", packetType == PacketType.RESET ? 0 : timer.getInGameTime(false));


        jsonObject.addProperty("currentSplitName", packetType != PacketType.RESET && completedSplits.size() > 0 ? completedSplits.get(completedSplits.size() - 1) : "");
        jsonObject.addProperty("currentSplitIndex", packetType == PacketType.RESET ? -1 : completedSplits.size());
        jsonObject.addProperty("timingMethod", 1);
        jsonObject.addProperty("currentDuration", packetType == PacketType.RESET ? 0 : (timer.getRealTimeAttack() - latestIgtPoint));
        jsonObject.addProperty("startTime", ("/Date(" + timer.getStartTime() + ")/").trim());
        jsonObject.addProperty("endTime", ("/Date(" + (timer.isCompleted() ? timer.getEndTime() : "0") + ")/").trim());
        jsonObject.addProperty("uploadKey", TheRunKeyHelper.UPLOAD_KEY);
        jsonObject.addProperty("isPaused", timer.isPaused());
        jsonObject.addProperty("isGameTimePaused", timer.isPaused());
        jsonObject.add("gameTimePauseTime", JsonNull.INSTANCE);
        jsonObject.add("totalPauseTime", JsonNull.INSTANCE);
        jsonObject.add("currentPauseTime", JsonNull.INSTANCE);
        jsonObject.addProperty("timePausedAt", timer.getLatestPauseTime());
        jsonObject.addProperty("wasJustResumed", packetType == PacketType.RESUME);
        jsonObject.add("runData", allSplits);

        return jsonObject;
    }

    public DOMSource convertXml() throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")));

        @Nullable TheRunCategory category = timer.getCategory().getTheRunCategory();
        if (category == null) throw new NullPointerException();

        List<TimerTimeline> timelines = timer.getTimelines();
        LinkedHashMap<String, String> splits = category.getSplitNameMap(timer);
        if (splits == null) return null;



        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        doc.setXmlStandalone(true);


        Element run = doc.createElement("Run");
        doc.appendChild(run);


        run.appendChild(doc.createElement("GameIcon"));


        Element gameName = doc.createElement("GameName");
        gameName.appendChild(doc.createTextNode(category.getGameName()));
        run.appendChild(gameName);


        Element categoryName = doc.createElement("CategoryName");
        categoryName.appendChild(doc.createTextNode(category.getCategoryName(timer)));
        run.appendChild(categoryName);


        run.appendChild(doc.createElement("LayoutPath"));


        Element metadata = doc.createElement("Metadata");

        Element runData = doc.createElement("Run");
        runData.setAttribute("id", "");
        metadata.appendChild(runData);

        Element platformData = doc.createElement("Platform");
        platformData.setAttribute("usesEmulator", "False");
        metadata.appendChild(platformData);

        metadata.appendChild(doc.createElement("Region"));

        metadata.appendChild(doc.createElement("Variables"));

        run.appendChild(metadata);


        Element offset = doc.createElement("Offset");
        offset.appendChild(doc.createTextNode("00:00:00"));
        run.appendChild(offset);


        Element attemptCount = doc.createElement("AttemptCount");
        attemptCount.appendChild(doc.createTextNode("1"));
        run.appendChild(attemptCount);


        Element attemptHistory = doc.createElement("AttemptHistory");

        Element attemptData = doc.createElement("Attempt");
        attemptData.setAttribute("id", "1");
        attemptData.setAttribute("started", dateFormat.format(new Date(timer.getStartTime())));
        attemptData.setAttribute("ended", dateFormat.format(new Date(timer.getEndTime())));
        attemptData.setAttribute("isStartedSynced", "true");
        attemptData.setAttribute("isEndedSynced", "true");
        Element realTimeData = doc.createElement("RealTime");
        realTimeData.appendChild(doc.createTextNode(InGameTimerUtils.timeToStringFormat(timer.getRealTimeAttack())));
        attemptData.appendChild(realTimeData);
        Element gameTimeData = doc.createElement("GameTime");
        gameTimeData.appendChild(doc.createTextNode(InGameTimerUtils.timeToStringFormat(timer.getInGameTime(false))));
        attemptData.appendChild(gameTimeData);
        attemptHistory.appendChild(attemptData);

        run.appendChild(attemptHistory);


        Element segments = doc.createElement("Segments");
        for (TimerTimeline timeline : timelines) {
            Element segment = doc.createElement("Segment");

            Element name = doc.createElement("Name");
            name.appendChild(doc.createTextNode(splits.get(timeline.getName())));
            segment.appendChild(name);

            Element realTimeSegment = doc.createElement("RealTime");
            realTimeSegment.appendChild(doc.createTextNode(InGameTimerUtils.timeToStringFormat(timeline.getRTA())));
            Element gameTimeSegment = doc.createElement("GameTime");
            gameTimeSegment.appendChild(doc.createTextNode(InGameTimerUtils.timeToStringFormat(timeline.getIGT())));

            segment.appendChild(doc.createElement("Icon"));
            Element splitTimes = doc.createElement("SplitTimes");
            Element pbSplit = doc.createElement("SplitTime");
            pbSplit.setAttribute("name", "Personal Best");
            pbSplit.appendChild(realTimeSegment.cloneNode(true));
            pbSplit.appendChild(gameTimeSegment.cloneNode(true));
            splitTimes.appendChild(pbSplit);
            segment.appendChild(splitTimes);
            segment.appendChild(doc.createElement("BestSegmentTime"));

            Element segmentHistory = doc.createElement("SegmentHistory");

            segmentHistory.appendChild(realTimeSegment);
            segmentHistory.appendChild(gameTimeSegment);

            segment.appendChild(segmentHistory);

            segments.appendChild(segment);
        }
        run.appendChild(segments);


        run.appendChild(doc.createElement("AutoSplitterSettings"));


        return new DOMSource(doc);
    }
}
