package com.redlimerl.speedrunigt.timer;

import com.google.gson.Gson;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.crypt.Crypto;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunCategory;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * In-game Timer class.
 * {@link TimerStatus}
 */
@SuppressWarnings("unused")
public class InGameTimer {

    @NotNull
    private static InGameTimer INSTANCE = new InGameTimer("");
    @NotNull
    private static InGameTimer COMPLETED_INSTANCE = new InGameTimer("");

    private static final String cryptKey = "faRQOs2GK5j863eP";

    @NotNull
    public static InGameTimer getInstance() { return INSTANCE; }

    private static final ArrayList<Consumer<InGameTimer>> onCompleteConsumers = new ArrayList<>();

    public static void onComplete(Consumer<InGameTimer> supplier) {
        onCompleteConsumers.add(supplier);
    }

    public InGameTimer(String worldName) {
        this(worldName, true);
    }
    public InGameTimer(String worldName, boolean isResettable) {
        this.worldName = worldName;
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
    private long rebaseRealTime = 0;
    private long excludedTime = 0; //for AA
    private long activateTicks = 0;
    private long leastTickTime = 0;
    private long leastStartTime = 0;

    private long leaveTime = 0;
    private int pauseCount = 0;

    //Logs
    private String firstInput = "";
    private final StringBuilder pauseLog = new StringBuilder();
    private final StringBuilder freezeLog = new StringBuilder();

    //For logging var
    private int loggerTicks = 0;
    private long loggerPausedTime = 0;

    @NotNull
    private TimerStatus status = TimerStatus.NONE;

    private final HashMap<Integer, Integer> moreData = new HashMap<>();

    /**
     * Start the Timer, Trigger when player to join(created) the world
     */
    public static void start(String worldName) {
        INSTANCE = new InGameTimer(worldName);
        INSTANCE.setCategory(SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY));
        INSTANCE.setPause(true, TimerStatus.IDLE);
        INSTANCE.isGlitched = SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE);
    }

    /**
     * Start the Timer, Trigger when player to join(created) the world
     */
    public static void reset() {
        if (INSTANCE.isCompleted || INSTANCE.getStatus() == TimerStatus.COMPLETED_LEGACY) return;

        INSTANCE = new InGameTimer(INSTANCE.worldName, false);
        INSTANCE.setCategory(RunCategories.CUSTOM);
        INSTANCE.setPause(true, TimerStatus.IDLE);
        INSTANCE.setPause(false);
        TimerPacketHandler.sendInitC2S(INSTANCE);
    }

    /**
     * End the Timer, Trigger when player leave
     */
    public static void end() {
        INSTANCE.setStatus(TimerStatus.NONE);
    }

    public static String timeToStringFormat(long time) {
        int seconds = ((int) (time / 1000)) % 60;
        int minutes = ((int) (time / 1000)) / 60;
        if (minutes > 59) {
            int hours = minutes / 60;
            minutes = minutes % 60;
            return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, time % 1000);
        } else {
            return String.format("%02d:%02d.%03d", minutes, seconds, time % 1000);
        }
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
        COMPLETED_INSTANCE = new Gson().fromJson(new Gson().toJson(INSTANCE) + "", InGameTimer.class);
        INSTANCE.isCompleted = true;
        InGameTimer timer = COMPLETED_INSTANCE;

        timer.endTime = endTime;
        timer.endIGTTime = timer.endTime - timer.leastTickTime;
        timer.setStatus(TimerStatus.COMPLETED_LEGACY);

        if (timer.isCoop) TimerPacketHandler.sendCompleteC2S(timer);

        if (INSTANCE.isServerIntegrated) {
            timer.pauseLog.append("Result > IGT ").append(timeToStringFormat(timer.getInGameTime()))
                    .append(", R-RTA ").append(timeToStringFormat(timer.getRealTimeAttack()))
                    .append(", RTA ").append(timeToStringFormat(timer.getRealTimeAttack(false)))
                    .append(", Counted Ticks: ").append(timer.activateTicks)
                    .append(", Total Ticks: ").append(timer.loggerTicks)
                    .append(", Rebased RTA Time: ").append(timeToStringFormat(timer.rebaseRealTime))
                    .append(", Rebased IGT Time: ").append(timeToStringFormat(timer.rebaseIGTime));
            if (timer.getCategory() == RunCategories.CUSTOM)
                timer.pauseLog.append(", Excluded RTA Time: ").append(timeToStringFormat(timer.excludedTime));

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
            String logInfo = "MC Version : " + SharedConstants.getGameVersion().getName() + "\r\n"
                    + "Timer Version : " + SpeedRunIGT.MOD_VERSION + "\r\n"
                    + "Run Date : " + simpleDateFormat.format(new Date()) + "\r\n"
                    + "====================\r\n";

            String worldName = INSTANCE.worldName;
            saveManagerThread.submit(() -> {
                try {
                    FileUtils.writeStringToFile(new File(SpeedRunIGT.WORLDS_PATH.resolve(worldName).toFile(), "igt_timer" + (timer.completeCount == 0 ? "" : "_"+timer.completeCount) + ".log"), logInfo + timer.firstInput + "\r\n" + timer.pauseLog, StandardCharsets.UTF_8);
                    FileUtils.writeStringToFile(new File(SpeedRunIGT.WORLDS_PATH.resolve(worldName).toFile(), "igt_freeze" + (timer.completeCount == 0 ? "" : "_"+timer.completeCount) + ".log"), logInfo + timer.freezeLog, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                    SpeedRunIGT.error("Failed to save timer logs :( RTA : " + timeToStringFormat(timer.getRealTimeAttack(false)) + " / IGT : " + timeToStringFormat(timer.getInGameTime()));
                }
            });
        }

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
        INSTANCE.setPause(true, TimerStatus.LEAVE);

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
                    return true;
                } catch (Throwable e) {
                    if (!isOld.isEmpty()) return false;
                    isOld = ".old";
                }
            } else if (SpeedRunOption.getOption(SpeedRunOptions.TIMER_START_GENERATED_WORLD) && isOld.isEmpty()) {
                InGameTimer.start(name);
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
        this.isCompleted = false;
        this.completeCount++;
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
        return this.getStatus() == TimerStatus.PAUSED || this.getStatus() == TimerStatus.IDLE;
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
        return getRealTimeAttack(true);
    }

    public long getRealTimeAttack(boolean withRebased) {
        return this.isCompleted && this != COMPLETED_INSTANCE ? COMPLETED_INSTANCE.getRealTimeAttack(withRebased) : this.getStatus() == TimerStatus.NONE ? 0 : this.getEndTime() - this.getStartTime() + (withRebased ? rebaseRealTime : 0) - this.excludedTime;
    }

    public long getInGameTime() { return getInGameTime(true); }

    public long getInGameTime(boolean smooth) {
        if (this.isCompleted && this != COMPLETED_INSTANCE) return COMPLETED_INSTANCE.getInGameTime(smooth);
        if (this.isCoop) return getRealTimeAttack();

        if (this.isGlitched && this.isServerIntegrated && MinecraftClient.getInstance().getServer() != null) {
            return MinecraftClient.getInstance().getServer().getPlayerManager().getPlayerList().get(0)
                    .getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) * 50L;
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
        boolean isRebasedRTA = false;

        //Rebase time (When a joined world or changed dimension)
        if (leastStartTime != 0 && leastTickTime != 0) {
            rebaseIGTime += MathHelper.clamp((leastStartTime - leastTickTime) * 1.0 / tickDelays, 0, 1) * 50.0;
            leastStartTime = 0;
        }
        if (!isPaused() && tickDelays < 49 && getInGameTime(false) > getRealTimeAttack()) {
            rebaseRealTime += Math.max(0, 50 - tickDelays);
            isRebasedRTA = true;
        }

        this.leastTickTime = currentTime;

        //Logger
        if (tickDelays != currentTime && Math.abs(50 - tickDelays) > 1 && !this.isCoop) {
            this.freezeLog.append(timeToStringFormat(getInGameTime(false))).append(" IGT, ").append(timeToStringFormat(getRealTimeAttack())).append(" RTA C, ")
                    .append(timeToStringFormat(getRealTimeAttack() - tickDelays)).append(" RTA P, ")
                    .append(tickDelays).append(" Tick delays ms, #").append(loggerTicks).append("(").append(activateTicks).append(") Tick");
            if (this.getStatus() == TimerStatus.IDLE) {
                this.freezeLog.append(", Waiting load or input");
            }
            if (isRebasedRTA && Math.max(0, 50 - tickDelays) > 0) {
                this.freezeLog.append(", Retimed RTA +").append(Math.max(0, 50 - tickDelays)).append("ms");
            }
            this.freezeLog.append("\r\n");
        }

        if (SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE) == SpeedRunOptions.TimerSaveInterval.TICKS) save();
    }

    public void setPause(boolean isPause) { this.setPause(isPause, TimerStatus.PAUSED); }
    public void setPause(boolean toPause, TimerStatus toStatus) {
        if (this.getStatus() == TimerStatus.COMPLETED_LEGACY || this.isCoop) return;

        if (toPause) {
            if (this.getStatus().getPause() <= toStatus.getPause()) {
                if (this.getStatus().getPause() < 1 && this.isStarted()) {
                    loggerPausedTime = getRealTimeAttack();
                    pauseCount++;
                    pauseLog.append("#").append(pauseCount).append(") ").append(timeToStringFormat(getInGameTime(false))).append(" IGT, ").append(timeToStringFormat(loggerPausedTime)).append(" RTA S, ");
                }
                this.setStatus(toStatus);
                if (SpeedRunOption.getOption(SpeedRunOptions.TIMER_DATA_AUTO_SAVE) == SpeedRunOptions.TimerSaveInterval.PAUSE && status != TimerStatus.LEAVE && this.isStarted()) save();
            }
        } else {
            if (this.isStarted()) {
                if (isPaused()) {
                    long nowTime = getRealTimeAttack();
                    pauseLog.append(timeToStringFormat(nowTime)).append(" RTA E, ").append(timeToStringFormat(nowTime - loggerPausedTime)).append(" Length (").append(leaveTime != 0 ? TimerStatus.LEAVE.getMessage() : getStatus().getMessage()).append(")\r\n");
                    if (this.getCategory() == RunCategories.ALL_ADVANCEMENTS && leaveTime != 0) excludedTime = System.currentTimeMillis() - leaveTime;
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
        if (server != null && server.getCurrentPlayerCount() > 1) {
            TimerPacketHandler.sendInitC2S(this);
        }
    }
}
