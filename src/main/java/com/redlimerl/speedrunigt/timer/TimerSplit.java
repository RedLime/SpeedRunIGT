package com.redlimerl.speedrunigt.timer;

import com.google.common.reflect.TypeToken;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.gui.GenericToast;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.SplitDisplayType;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class TimerSplit {

    public static LinkedHashMap<String, String> SPLIT_DATA = new LinkedHashMap<>();
    private static final Path BEST_SPLITS_PATH = SpeedRunIGT.getMainPath().resolve("splits.txt");
    public static void load() {
        File splitFile = BEST_SPLITS_PATH.toFile();
        if (splitFile.exists()) {
            try {
                //noinspection UnstableApiUsage
                SPLIT_DATA = SpeedRunIGT.GSON.fromJson(FileUtils.readFileToString(splitFile, StandardCharsets.UTF_8), new TypeToken<LinkedHashMap<String, String>>() {}.getType());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void save() {
        new Thread(() -> {
            try {
                FileUtils.writeStringToFile(BEST_SPLITS_PATH.toFile(), SpeedRunIGT.GSON.toJson(SPLIT_DATA), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public enum SplitType {
        ENTER_NETHER("advancements.story.enter_the_nether.title", "en"), ENTER_END("advancements.story.enter_the_end.title", "ee"),
        ENTER_STRONG_HOLD("advancements.story.follow_ender_eye.title", "es"), ENTER_FORTRESS("advancements.nether.find_fortress.title", "ef"),
        ENTER_BASTION("advancements.nether.find_bastion.title", "eb"), COMPLETE("speedrunigt.split.complete_run", "c");

        private final String titleKey;
        private final String code;

        SplitType(String titleKey, String code) {
            this.titleKey = titleKey;
            this.code = code;
        }

        public String getTitleKey() {
            return titleKey;
        }
    }

    private final long seed;
    private final boolean isSetSeed;
    private RunCategory runCategory;
    private final LinkedHashMap<SplitType, Long> splitTimeline = new LinkedHashMap<>();


    public TimerSplit(long seed, boolean isSetSeed) {
        this.seed = seed;
        this.isSetSeed = isSetSeed;
    }

    public LinkedHashMap<SplitType, Long> getSplitTimeline() {
        return splitTimeline;
    }

    private void updateSplit(SplitType splitType, Long time, boolean isBest) {
        getSplitTimeline().put(splitType, time);
        if (splitType == SplitType.COMPLETE && isBest) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<SplitType, Long> split : getSplitTimeline().entrySet()) {
                stringBuilder.append(split.getKey().code).append("-");
            }
            SPLIT_DATA.put(this.getIdentifyString() + ":" + stringBuilder.substring(0, stringBuilder.length() - 1), this.getTimelineString());
            save();
        }
    }

    public RunCategory getRunCategory() {
        return runCategory;
    }

    public void setRunCategory(RunCategory runCategory) {
        this.runCategory = runCategory;
    }

    public long getSeed() {
        return seed;
    }

    public boolean isSetSeed() {
        return isSetSeed;
    }

    public String getIdentifyString() {
        return (isSetSeed() ? getSeed() : "-") + ":" + getRunCategory().name() + ":" + SharedConstants.getGameVersion().getName();
    }

    public String getTimelineString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<SplitType, Long> split : getSplitTimeline().entrySet()) {
            stringBuilder.append(split.getKey().name()).append("|").append(split.getValue()).append(",");
        }
        return stringBuilder.substring(0, stringBuilder.length() - (getSplitTimeline().entrySet().size() > 0 ? 1 : 0));
    }

    private String getTimelineRegex(SplitType splitType) {
        StringBuilder stringBuilder = new StringBuilder("^(");
        for (Map.Entry<SplitType, Long> split : getSplitTimeline().entrySet()) {
            // (ENTER_END)+\|+[0-9]*
            stringBuilder.append("(").append(split.getKey().name()).append(")+\\|+[0-9]*+\\,+");
        }
        stringBuilder.append("(").append(splitType.name()).append(")+\\|+[0-9]*+.*$)");
        return stringBuilder.toString();
    }

    public void tryUpdateSplit(SplitType splitType, Long igt) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        SplitDisplayType splitDisplayType = SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE);

        long bestTime = 0L;
        for (Map.Entry<String, String> splitData : SPLIT_DATA.entrySet()) {
            if (splitData.getKey().startsWith(this.getIdentifyString()) && splitData.getValue().matches(this.getTimelineRegex(splitType))) {
                String[] timelines = splitData.getValue().split(",");
                for (String timeline : timelines) {
                    String[] timelineData = timeline.split("\\|");
                    if (SplitType.valueOf(timelineData[0]) == splitType) {
                        long l1 = Long.parseLong(timelineData[1]);
                        if (l1 < bestTime || bestTime == 0) bestTime = l1;
                    }
                }
            }
        }

        String timeString = "Time: " + InGameTimer.timeToStringFormat(igt) + (bestTime == 0L ? "" : " " + ((bestTime >= igt ? "§a[-" : "§c[+") + InGameTimer.timeToStringFormat(Math.abs(bestTime - igt)) + "]"));
        String titleString = splitType == SplitType.COMPLETE ? (SharedConstants.getGameVersion().getName() + " " + getRunCategory().getText().getString() + " " + (isSetSeed() ? "SSG" : "RSG")) : I18n.translate(splitType.titleKey);
        if (splitDisplayType == SplitDisplayType.MESSAGE) {
            client.player.sendMessage(new LiteralText("§f§l▶ §e" + titleString), false);
            client.player.sendMessage(new LiteralText("§f§l▶ §f- " + timeString), false);
        } else if (splitDisplayType == SplitDisplayType.TOAST) {
            client.getToastManager().add(new GenericToast("§e" + titleString, timeString, new ItemStack(Items.CLOCK)));
        }
        this.updateSplit(splitType, igt, bestTime >= igt || bestTime == 0L);
    }
}
