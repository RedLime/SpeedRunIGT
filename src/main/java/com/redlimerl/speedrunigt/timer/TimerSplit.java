package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.SplitDisplayType;
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

public class TimerSplit {

    private static final File SPLITS_DIR = SpeedRunIGT.getMainPath().resolve("splits").toFile();

    public static ArrayList<TimerSplit> SPLIT_DATA = new ArrayList<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void load() {
        SPLITS_DIR.mkdirs();
        SPLIT_DATA.clear();

        File[] files = SPLITS_DIR.listFiles();
        if (files != null) {
            try {
                for (File file : files) {
                    SPLIT_DATA.add(SpeedRunIGT.GSON.fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), TimerSplit.class));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void save() {
        new Thread(() -> {
            try {
                FileUtils.writeStringToFile(new File(SPLITS_DIR, this.getFileName()), SpeedRunIGT.GSON.toJson(this), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void delete() {
        SPLIT_DATA.remove(this);
        new Thread(() -> {
            try {
                FileUtils.forceDelete(new File(SPLITS_DIR, this.getFileName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public enum SplitType {
        ENTER_NETHER("advancements.story.enter_the_nether.title"), ENTER_END("advancements.story.enter_the_end.title"),
        ENTER_STRONG_HOLD("advancements.story.follow_ender_eye.title"), ENTER_FORTRESS("advancements.nether.find_fortress.title"),
        ENTER_BASTION("advancements.nether.find_bastion.title"), COMPLETE("speedrunigt.split.complete_run");

        private final String titleKey;

        SplitType(String titleKey) {
            this.titleKey = titleKey;
        }

        public String getTitleKey() {
            return titleKey;
        }
    }

    private final String seed;
    private final RunType runType;
    private boolean coop = false;
    private long timestamp = System.currentTimeMillis();
    private long resultTime = 0;
    private RunCategory runCategory;
    private String version = "unknown";
    private final LinkedHashMap<SplitType, Long> splitTimeline = new LinkedHashMap<>();


    public TimerSplit(long seed, RunType runType, RunCategory runCategory) {
        this(String.valueOf(seed), runType, runCategory);
    }
    public TimerSplit(String seed, RunType runType, RunCategory runCategory) {
        this.seed = seed;
        this.runType = runType;
        this.runCategory = runCategory;
    }

    public LinkedHashMap<SplitType, Long> getSplitTimeline() {
        return splitTimeline;
    }

    private void updateSplit(SplitType splitType, Long time) {
        if (splitType == SplitType.COMPLETE) resultTime = time;
        getSplitTimeline().put(splitType, time);
    }

    public void completeSplit(boolean isCoop) {
        if (getRunCategory() == RunCategory.CUSTOM) return;

        version = SharedConstants.getGameVersion().getName();
        timestamp = System.currentTimeMillis();
        coop = isCoop;
        SPLIT_DATA.add(this);
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
        return runCategory;
    }

    public boolean isCoop() {
        return coop;
    }

    public void setRunCategory(RunCategory runCategory) {
        this.runCategory = runCategory;
    }

    public String getSeed() {
        return seed;
    }

    public RunType getRunType() {
        return runType;
    }

    public String getIdentifyString() {
        return (getRunType() == RunType.SET_SEED ? getSeed() : getRunType().name()) + ":" + getRunCategory().name() + ":" + getVersion() + ":" + this.coop;
    }
    public String getTimelineString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<SplitType, Long> split : getSplitTimeline().entrySet()) {
            stringBuilder.append(split.getKey().name()).append("|");
        }
        return stringBuilder.substring(0, stringBuilder.length() - (getSplitTimeline().entrySet().size() > 0 ? 1 : 0));
    }
    public String getFileName() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(timestamp)) + ".spl";
    }

    public void tryUpdateSplit(SplitType splitType, Long igt) {
        tryUpdateSplit(splitType, igt, true);
    }
    @SuppressWarnings("unused")
    public void tryUpdateSplit(SplitType splitType, Long igt, boolean sendPacket) {
        if (getSplitTimeline().containsKey(splitType)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        SplitDisplayType splitDisplayType = SpeedRunOption.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE);

        long bestTime = 0L;
        for (TimerSplit splits : SPLIT_DATA) {
            if (splits.getIdentifyString().startsWith(this.getIdentifyString())
                    && splits.getSplitTimeline().containsKey(splitType)
                    && splits.getTimelineString().startsWith(this.getTimelineString())) {
                long time = splits.getSplitTimeline().get(splitType);
                if (time < bestTime || bestTime == 0) bestTime = time;
            }
        }


        String timeString = "Time: " + InGameTimer.timeToStringFormat(igt) + (bestTime == 0L ? "" : " " + ((bestTime >= igt ? "§a[-" : "§c[+") + InGameTimer.timeToStringFormat(Math.abs(bestTime - igt)) + "]"));
        String titleString = splitType == SplitType.COMPLETE ? (SharedConstants.getGameVersion().getName() + " " + getRunCategory().getText().getString() + " " + getRunType().name()) : I18n.translate(splitType.titleKey);
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
