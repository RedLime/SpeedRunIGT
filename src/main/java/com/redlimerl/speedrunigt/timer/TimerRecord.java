package com.redlimerl.speedrunigt.timer;

import com.google.gson.JsonSyntaxException;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.SplitDisplayType;
import com.redlimerl.speedrunigt.timer.running.*;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class TimerRecord {

    private static final File RECORDS_DIR = SpeedRunIGT.getGlobalPath().resolve("splits").toFile();

    public static ArrayList<TimerRecord> RECORD_LIST = new ArrayList<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void load() {
        RECORDS_DIR.mkdirs();
        RECORD_LIST.clear();

        File[] files = RECORDS_DIR.listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    RECORD_LIST.add(SpeedRunIGT.GSON.fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), TimerRecord.class));
                } catch (JsonSyntaxException e) {
                    SpeedRunIGT.error("Failed read to "+file.getName()+"! is it not timer record file? try delete it.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void save() {
        new Thread(() -> {
            try {
                FileUtils.writeStringToFile(new File(RECORDS_DIR, this.getFileName()), SpeedRunIGT.GSON.toJson(this), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void delete() {
        RECORD_LIST.remove(this);
        new Thread(() -> {
            try {
                FileUtils.forceDelete(new File(RECORDS_DIR, this.getFileName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private final String seed;
    private final RunType runType;
    private boolean coop = false;
    private long timestamp = System.currentTimeMillis();
    private long resultTime = 0;
    private boolean isGlitched = false;
    private String runCategory;
    private String version = "unknown";
    private final LinkedHashMap<String, Long> splitTimeline = new LinkedHashMap<>();


    public TimerRecord(long seed, RunType runType, RunCategory runCategory) {
        this(String.valueOf(seed), runType, runCategory);
    }
    public TimerRecord(String seed, RunType runType, RunCategory runCategory) {
        this.seed = seed;
        this.runType = runType;
        this.runCategory = runCategory.getID();
    }

    public Map<String, Long> getSplitTimeline() {
        return splitTimeline;
    }

    private void updateSplit(RunSplitType splitType, Long time) {
        if (splitType == RunSplitTypes.COMPLETE) resultTime = time;
        getSplitTimeline().put(splitType.getID(), time);
    }

    public void completeSplit(boolean isCoop, boolean isGlitched) {
        if (getRunCategory() == RunCategories.CUSTOM) return;

        version = SharedConstants.getGameVersion().getName();
        timestamp = System.currentTimeMillis();
        coop = isCoop;
        this.isGlitched = isGlitched;
        RECORD_LIST.add(this);
        save();
    }

    public String getVersion() {
        return version;
    }

    public long getResultTime() {
        return resultTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public RunCategory getRunCategory() {
        return RunCategory.getCategory(runCategory);
    }

    public boolean isCoop() {
        return coop;
    }

    public boolean isGlitched() {
        return isGlitched;
    }

    public void setRunCategory(RunCategory runCategory) {
        this.runCategory = runCategory.getID();
    }

    public String getSeed() {
        return seed;
    }

    public RunType getRunType() {
        return runType;
    }

    public String getIdentifyString() {
        return (getRunType() == RunType.SET_SEED ? getSeed() : getRunType().name()) + ":" + getRunCategory().getID() + ":" + getVersion() + ":" + this.coop;
    }
    public String getTimelineString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Long> split : getSplitTimeline().entrySet()) {
            stringBuilder.append(split.getKey()).append("|");
        }
        return stringBuilder.substring(0, stringBuilder.length() - (getSplitTimeline().entrySet().size() > 0 ? 1 : 0));
    }
    public String getFileName() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(timestamp)) + ".spl";
    }

    public void tryUpdateSplit(RunSplitType splitType, Long igt) {
        tryUpdateSplit(splitType, igt, true);
    }
    @SuppressWarnings("unused")
    public void tryUpdateSplit(RunSplitType splitType, Long igt, boolean sendPacket) {
        if (getSplitTimeline().containsKey(splitType.getID())) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        SplitDisplayType splitDisplayType = SpeedRunOption.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE);

        long bestTime = 0L;
        for (TimerRecord splits : RECORD_LIST) {
            if (splits.getIdentifyString().startsWith(this.getIdentifyString())
                    && splits.getSplitTimeline().containsKey(splitType.getID())
                    && splits.getTimelineString().startsWith(this.getTimelineString())) {
                long time = splits.getSplitTimeline().get(splitType.getID());
                if (time < bestTime || bestTime == 0) bestTime = time;
            }
        }


        String timeString = "Time: " + InGameTimer.timeToStringFormat(igt) + (bestTime == 0L ? "" : " " + ((bestTime >= igt ? "§a[-" : "§c[+") + InGameTimer.timeToStringFormat(Math.abs(bestTime - igt)) + "]"));
        String titleString = splitType == RunSplitTypes.COMPLETE ? (SharedConstants.getGameVersion().getName() + " " + getRunCategory().getText().asFormattedString() + " " + getRunType().name()) : I18n.translate(splitType.getTranslateKey());
        if (splitDisplayType == SplitDisplayType.MESSAGE) {
            //client.player.sendMessage(new LiteralText("§f§l▶ §e" + titleString), false);
            //client.player.sendMessage(new LiteralText("§f§l▶ §f- " + timeString), false);
            SpeedRunIGT.debug("split message");
        } else if (splitDisplayType == SplitDisplayType.TOAST) {
            //client.getToastManager().add(new GenericToast("§e" + titleString, timeString, new ItemStack(Items.CLOCK)));
            SpeedRunIGT.debug("split toast");
        }

        this.updateSplit(splitType, igt);

        if (InGameTimer.getInstance().isCoop() && sendPacket) {
            TimerPacketHandler.sendSplitC2S(splitType, igt);
        }
    }
}
