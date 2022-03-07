package com.redlimerl.speedrunigt.timer;

import com.google.gson.Gson;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.crypt.Crypto;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.logs.TimerPauseLog;
import com.redlimerl.speedrunigt.timer.logs.TimerTickLog;
import com.redlimerl.speedrunigt.timer.logs.TimerTimeline;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static com.redlimerl.speedrunigt.timer.InGameTimerUtils.timeToStringFormat;

/**
 * In-game Timer class.
 * {@link TimerStatus}
 */
@SuppressWarnings("unused")
public class InGameTimer {

    @NotNull
    private static InGameTimer INSTANCE = new InGameTimer("", "", false);
    @NotNull
    private static InGameTimer COMPLETED_INSTANCE = new InGameTimer("", "", false);

    private static final String cryptKey = "faRQOs2GK5j863eP";

    @NotNull
    public static InGameTimer getInstance() { return INSTANCE; }

    private static final ArrayList<Consumer<InGameTimer>> onCompleteConsumers = new ArrayList<>();

    public static void onComplete(Consumer<InGameTimer> supplier) {
        onCompleteConsumers.add(supplier);
    }

    public InGameTimer(String worldName, String seedName, boolean isSetSeed) {
        this(worldName, true, seedName, isSetSeed);
    }
    public InGameTimer(String worldName, boolean isResettable, String seedName, boolean isSetSeed) {
        this.worldName = worldName;
        this.seedName = seedName;
        this.isSetSeed = isSetSeed;
        this.isResettable = isResettable;
    }

    private final String worldName;
    private String category = RunCategories.ANY.getID();
    private final boolean isResettable;
    private boolean isCompleted = false;
    boolean isServerIntegrated = true;
    boolean isCoop = false;
    private boolean isGlitched = false;
    private int completeCount = 0;

    //Timer time
    long startTime = 0;
    long endTime = 0;
    private long endIGTTime = 0;
    private long rebaseIGTime = 0;
    private long excludedTime = 0; //for AA
    private long activateTicks = 0;
    private long leastTickTime = 0;
    private long leastStartTime = 0;

    private long leaveTime = 0;
    private int pauseCount = 0;

    //Logs
    private String firstInput = "";
    private final ArrayList<TimerPauseLog> pauseLogList = new ArrayList<>();
    private final ArrayList<TimerTickLog> freezeLogList = new ArrayList<>();

    //For logging var
    private int loggerTicks = 0;
    private long loggerPausedTime = 0;
    private String prevPauseReason = "";

    //For record
    private final String seedName;
    private final boolean isSetSeed;
    private final ArrayList<TimerTimeline> timelines = new ArrayList<>();
    private boolean isHardcore = false;

    @NotNull
    private TimerStatus status = TimerStatus.NONE;

    private final HashMap<Integer, Integer> moreData = new HashMap<>();

    /**
     * Start the Timer, Trigger when player to join(created) the world
     */
    public static void start(String worldName, String seedName, boolean isSetSeed) {
        INSTANCE = new InGameTimer(worldName, seedName, isSetSeed);
        INSTANCE.setCategory(SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY));
        INSTANCE.setPause(true, TimerStatus.IDLE, "startup");
        INSTANCE.isGlitched = SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE);
    }

    /**
     * Start the Timer, Trigger when player to join(created) the world
     */
    public static void reset() {
        if (INSTANCE.isCompleted || INSTANCE.getStatus() == TimerStatus.COMPLETED_LEGACY) return;

        INSTANCE = new InGameTimer(INSTANCE.worldName, false, INSTANCE.seedName, INSTANCE.isSetSeed);
        INSTANCE.setCategory(RunCategories.CUSTOM);
        INSTANCE.setPause(true, TimerStatus.IDLE, "reset");
        INSTANCE.setPause(false, "reset");
        TimerPacketHandler.sendInitC2S(INSTANCE);
    }

    /**
     * End the Timer, Trigger when player leave
     */
    public static void end() {
        INSTANCE.setStatus(TimerStatus.NONE);
    }

    /**
     * End the Timer, Trigger when Complete Ender Dragon
     */
    public static void complete() {
        complete(System.currentTimeMillis());
    }

    /**
     * End the Timer, Trigger when Complete Ender Dragon
     */
    static void complete(long endTime) {
        if (INSTANCE.isCompleted || !INSTANCE.isStarted()) return;

        // Init additional data
        INSTANCE.isHardcore = InGameTimerUtils.isHardcoreWorld();

        COMPLETED_INSTANCE = new Gson().fromJson(new Gson().toJson(INSTANCE) + "", InGameTimer.class);
        INSTANCE.isCompleted = true;
        InGameTimer timer = COMPLETED_INSTANCE;

        timer.endTime = endTime;
        timer.endIGTTime = timer.endTime - timer.leastTickTime;
        timer.setStatus(TimerStatus.COMPLETED_LEGACY);

        if (timer.isCoop) TimerPacketHandler.sendCompleteC2S(timer);

        if (INSTANCE.isServerIntegrated) {
            StringBuilder resultLog = new StringBuilder();
            resultLog.append("Result > IGT ").append(timeToStringFormat(timer.getInGameTime()))
                    .append(", RTA ").append(timeToStringFormat(timer.getRealTimeAttack()))
                    .append(", Counted Ticks: ").append(timer.activateTicks)
                    .append(", Total Ticks: ").append(timer.loggerTicks)
                    .append(", Rebased IGT Time: ").append(timeToStringFormat(timer.rebaseIGTime));
            if (timer.getCategory() == RunCategories.CUSTOM)
                resultLog.append(", Excluded RTA Time: ").append(timeToStringFormat(timer.excludedTime));

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
            String logInfo = "MC Version : " + InGameTimerUtils.getMinecraftVersion() + "\r\n"
                    + "Timer Version : " + SpeedRunIGT.MOD_VERSION + "\r\n"
                    + "Run Date : " + simpleDateFormat.format(new Date()) + "\r\n"
                    + "====================\r\n";

            String worldName = INSTANCE.worldName;
            saveManagerThread.submit(() -> {
                try {
                    FileUtils.writeStringToFile(new File(SpeedRunIGT.WORLDS_PATH.resolve(worldName).toFile(), "igt_timer" + (timer.completeCount == 0 ? "" : "_"+timer.completeCount) + ".log"), logInfo + timer.firstInput + "\n" + InGameTimerUtils.logListToString(timer.pauseLogList) + resultLog, StandardCharsets.UTF_8);
                    FileUtils.writeStringToFile(new File(SpeedRunIGT.WORLDS_PATH.resolve(worldName).toFile(), "igt_freeze" + (timer.completeCount == 0 ? "" : "_"+timer.completeCount) + ".log"), logInfo + InGameTimerUtils.logListToString(timer.freezeLogList), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                    SpeedRunIGT.error("Failed to save timer logs :( RTA : " + timeToStringFormat(timer.getRealTimeAttack()) + " / IGT : " + timeToStringFormat(timer.getInGameTime()));
                }
            });
        }

        String recordString = SpeedRunIGT.PRETTY_GSON.toJson(InGameTimerUtils.convertTimelineJson(INSTANCE));
        saveManagerThread.submit(() -> {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
            try {
                FileUtils.writeStringToFile(new File(SpeedRunIGT.getRecordsPath().toFile(), simpleDateFormat.format(new Date()) + ".json"), recordString, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                SpeedRunIGT.error("Failed to write timer record :(");
            }
        });

        for (Consumer<InGameTimer> onCompleteConsumer : onCompleteConsumers) {
            try {
                onCompleteConsumer.accept(timer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void leave() {
        if (!INSTANCE.isServerIntegrated) return;

        INSTANCE.leaveTime = System.currentTimeMillis();
        INSTANCE.pauseCount = 0;
        INSTANCE.setPause(true, TimerStatus.LEAVE, "leave the world");

        save(true);
    }

    private static boolean waitingSaveTask = false;
    private static final ExecutorService saveManagerThread = Executors.newFixedThreadPool(2);
    private static void save() { save(false); }
    private static void save(boolean withLeave) {
        if (waitingSaveTask || saveManagerThread.isShutdown() || saveManagerThread.isTerminated() || !INSTANCE.isServerIntegrated) return;

        String worldName = INSTANCE.worldName, timerData = SpeedRunIGT.GSON.toJson(INSTANCE) + "", completeData = SpeedRunIGT.GSON.toJson(COMPLETED_INSTANCE) + "";
        File worldDir = InGameTimerUtils.getWorldSavePath(worldName).toFile();
        if (withLeave) end();

        waitingSaveTask = true;
        saveManagerThread.submit(() -> {
            try {
                if (worldDir.exists()) {
                    File timerFile = new File(worldDir, "timer.igt");
                    File timerCompleteFile = new File(worldDir, "timer.c.igt");
                    File oldTimerFile = new File(worldDir, "timer.igt.old");
                    File oldTimerCompleteFile = new File(worldDir, "timer.c.igt.old");

                    // Old data backup
                    if (timerFile.exists()) {
                        if (oldTimerFile.exists()) FileUtils.forceDelete(oldTimerFile);
                        FileUtils.moveFile(timerFile, oldTimerFile);
                        if (timerCompleteFile.exists()) {
                            if (oldTimerCompleteFile.exists()) FileUtils.forceDelete(oldTimerCompleteFile);
                            FileUtils.moveFile(timerCompleteFile, oldTimerCompleteFile);
                        }
                        else if (oldTimerCompleteFile.exists()) FileUtils.deleteQuietly(oldTimerCompleteFile);
                    }

                    // Save data
                    FileUtils.writeStringToFile(timerFile, Crypto.encrypt(timerData, cryptKey), StandardCharsets.UTF_8);
                    if (INSTANCE.isCompleted) FileUtils.writeStringToFile(timerCompleteFile, Crypto.encrypt(completeData, cryptKey), StandardCharsets.UTF_8);

                    waitingSaveTask = false;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                SpeedRunIGT.error("Failed to save timer data's :(");
            }
        });
    }

    public static boolean load(String name) {
        Path worldPath = InGameTimerUtils.getWorldSavePath(name);
        String isOld = "";
        while (true) {
            File file = new File(worldPath.toFile(), "timer.igt"+isOld);
            File completeFile = new File(worldPath.toFile(), "timer.c.igt"+isOld);
            if (file.exists()) {
                try {
                    INSTANCE = SpeedRunIGT.GSON.fromJson(Crypto.decrypt(FileUtils.readFileToString(file, StandardCharsets.UTF_8), cryptKey), InGameTimer.class);
                    if (completeFile.exists()) COMPLETED_INSTANCE = SpeedRunIGT.GSON.fromJson(Crypto.decrypt(FileUtils.readFileToString(completeFile, StandardCharsets.UTF_8), cryptKey), InGameTimer.class);
                    SpeedRunIGT.debug("Loaded Timer Saved Data! " + isOld);
                    INSTANCE.setStatus(TimerStatus.LEAVE);
                    return true;
                } catch (Throwable e) {
                    if (!isOld.isEmpty()) return false;
                    isOld = ".old";
                }
            } else if (SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD) && isOld.isEmpty()) {
                InGameTimer.start(name, name, true);
                return true;
            } else return false;
        }
    }



    public @NotNull RunCategory getCategory() {
        return RunCategory.getCategory(category);
    }

    public void setCategory(RunCategory category) {
        this.category = category.getID();
    }

    public boolean isCoop() {
        return isCoop;
    }

    public long getTicks() {
        return this.activateTicks;
    }

    public int getMoreData(int key) {
        return moreData.getOrDefault(key, 0);
    }

    public void updateMoreData(int key, int value) {
        moreData.put(key, value);
    }

    public @NotNull TimerStatus getStatus() {
        return status;
    }

    public void setStatus(@NotNull TimerStatus status) {
        if (this.getStatus() == TimerStatus.COMPLETED_LEGACY && status != TimerStatus.NONE) return;
        this.status = status;
    }

    public void setUncompleted() {
        if (this.isCompleted) this.completeCount++;
        this.isCompleted = false;
        sendTimerStartPacket();
    }

    public boolean isCompleted() {
        return this.isCompleted && this.getStatus() != TimerStatus.COMPLETED_LEGACY;
    }

    public int getPauseCount() {
        return pauseCount;
    }

    public boolean isStarted() {
        return this.startTime != 0;
    }

    public boolean isPaused() {
        return this.getStatus() != TimerStatus.COMPLETED_LEGACY && this.getStatus().getPause() > 0;
    }

    public boolean isPlaying() {
        return !this.isPaused() && this.getStatus() != TimerStatus.COMPLETED_LEGACY;
    }

    private long getEndTime() {
        return this.getStatus() == TimerStatus.COMPLETED_LEGACY ? this.endTime : System.currentTimeMillis();
    }

    public long getStartTime() {
        return this.isStarted() ? startTime : System.currentTimeMillis();
    }

    public long getRealTimeAttack() {
        return this.isCompleted && this != COMPLETED_INSTANCE ? COMPLETED_INSTANCE.getRealTimeAttack() : this.getStatus() == TimerStatus.NONE ? 0 : this.getEndTime() - this.getStartTime() - this.excludedTime;
    }

    public long getInGameTime() { return getInGameTime(true); }

    public long getInGameTime(boolean smooth) {
        if (this.isCompleted && this != COMPLETED_INSTANCE) return COMPLETED_INSTANCE.getInGameTime(smooth);
        if (this.isCoop) return getRealTimeAttack();

        if (this.isGlitched && this.isServerIntegrated && MinecraftClient.getInstance().getServer() != null) {
            ServerPlayerEntity player = (ServerPlayerEntity) MinecraftClient.getInstance().getServer().getPlayerManager().players.get(0);
            return player.getStatHandler().method_1729(Stats.MINUTES_PLAYED) * 50L;
        }

        long ms = System.currentTimeMillis();
        return !isStarted() ? 0 :
                (this.getTicks() * 50L) // Tick Based
                        + Math.min(50, smooth && isPlaying() && this.leastTickTime != 0 ? ms - this.leastTickTime : 0) // More smooth timer in playing
                        - this.rebaseIGTime // Subtract Rebased time
                        + this.endIGTTime;
    }

    private long firstRenderedTime = 0;
    public void updateFirstInput() {
        if (firstInput.isEmpty() && !SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT)) {
            firstInput = "First Input: IGT " + timeToStringFormat(getInGameTime(false)) + (leastTickTime == 0 ? "" : " (+ " + (System.currentTimeMillis() - this.leastTickTime) + "ms)") + ", RTA " + timeToStringFormat(getRealTimeAttack());
        }
        if (firstRenderedTime != 0) {
            firstInput = "First World Rendered: " + (System.currentTimeMillis() - this.firstRenderedTime) + "ms before first input";
        }
    }

    public void updateFirstRendered() {
        if (firstInput.isEmpty() && firstRenderedTime == 0 && SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT)) {
            firstRenderedTime = System.currentTimeMillis();
        }
    }


    public void tick() {
        if (this.getStatus() == TimerStatus.COMPLETED_LEGACY) return;

        if (isPlaying()) {
            this.activateTicks++;
        }
        this.loggerTicks++;

        long currentTime = System.currentTimeMillis();
        long tickDelays = currentTime - leastTickTime;

        //Rebase time (When a joined world or changed dimension)
        if (leastStartTime != 0 && leastTickTime != 0) {
            rebaseIGTime += MathHelper.clamp((leastStartTime - leastTickTime) * 1.0 / tickDelays, 0, 1) * 50.0;
            leastStartTime = 0;
        }

        this.leastTickTime = currentTime;

        //Logger
        if (tickDelays != currentTime && Math.abs(50 - tickDelays) > 4 && !this.isCoop()) {
            this.freezeLogList.add(new TimerTickLog(activateTicks, loggerTicks, getRealTimeAttack(),
                    tickDelays, getInGameTime(false), this.getStatus() == TimerStatus.IDLE));
        }

        if (SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE) == SpeedRunOptions.TimerSaveInterval.TICKS) save();
    }

    public void setPause(boolean isPause, String reason) { this.setPause(isPause, TimerStatus.PAUSED, reason); }
    public void setPause(boolean toPause, TimerStatus toStatus, String reason) {
        if (this.getStatus() == TimerStatus.COMPLETED_LEGACY || this.isCoop) return;

        if (toPause) {
            if (this.getStatus().getPause() <= toStatus.getPause()) {
                if (this.getStatus().getPause() < 1 && this.isStarted()) {
                    loggerPausedTime = getRealTimeAttack();
                    prevPauseReason = reason;
                    pauseCount++;
                }
                this.setStatus(toStatus);
                if (SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE) == SpeedRunOptions.TimerSaveInterval.PAUSE && status != TimerStatus.LEAVE && this.isStarted()) save();
            }
        } else {
            if (this.isStarted()) {
                if (isPaused()) {
                    long nowTime = getRealTimeAttack();
                    this.pauseLogList.add(new TimerPauseLog(prevPauseReason, reason, getInGameTime(false), getRealTimeAttack(), nowTime - loggerPausedTime, pauseCount));
                    if (this.getCategory() == RunCategories.ALL_ACHIEVEMENTS && leaveTime != 0) excludedTime = System.currentTimeMillis() - leaveTime;
                    leaveTime = 0;
                }
                if (this.getStatus() == TimerStatus.IDLE && loggerTicks != 0) {
                    leastStartTime = System.currentTimeMillis();
                }
            } else {
                startTime = System.currentTimeMillis();
                if (this.isGlitched) save();
                if (loggerTicks != 0) leastStartTime = startTime;
                sendTimerStartPacket();
            }
            this.setStatus(TimerStatus.RUNNING);
        }
    }

    public boolean isResettable() {
        return isResettable || SpeedRunOption.getOption(SpeedRunOptions.TIMER_LIMITLESS_RESET);
    }

    private void sendTimerStartPacket() {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (this.getStatus() != TimerStatus.NONE && server != null && server.getCurrentPlayerCount() > 1) {
            TimerPacketHandler.sendInitC2S(this);
        }
    }

    public boolean tryInsertNewTimeline(String name) {
        for (TimerTimeline timeline : timelines) {
            if (Objects.equals(timeline.getName(), name)) return false;
        }
        return timelines.add(new TimerTimeline(name, getInGameTime(false), getRealTimeAttack()));
    }

    public List<TimerTimeline> getTimelines() {
        return timelines;
    }

    public String getSeedName() {
        return seedName;
    }

    public boolean isSetSeed() {
        return isSetSeed;
    }

    public boolean isHardcore() {
        return isHardcore;
    }

    public boolean isLegacyIGT() {
        return isGlitched;
    }
}
