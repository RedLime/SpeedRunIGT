package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.crypt.Crypto;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import net.minecraft.SharedConstants;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * In-game Timer class.
 * {@link TimerStatus}
 */
@SuppressWarnings("unused")
public class InGameTimer {

    @NotNull
    private static InGameTimer INSTANCE = new InGameTimer();

    public static InGameTimer getInstance() { return INSTANCE; }
    public static String currentWorldName = "";
    public static boolean checkingWorld = true;

    private static final ArrayList<Consumer<InGameTimer>> onCompleteConsumers = new ArrayList<>();

    public static void onComplete(Consumer<InGameTimer> supplier) {
        onCompleteConsumers.add(supplier);
    }

    public InGameTimer() {
        this(true);
    }
    public InGameTimer(boolean isResettable) {
        this.isResettable = isResettable;
    }

    private RunCategory category = RunCategory.ANY;
    private final boolean isResettable;

    //Timer time
    private long startTime = 0;
    private long endTime = 0;
    private long endIGTTime = 0;
    private long rebaseIGTime = 0;
    private long rebaseRealTime = 0;
    private long excludedTime = 0; //for AA
    private long activateTicks = 0;
    private long leastTickTime = 0;
    private long leastStartTime = 0;

    private long leaveTime = 0;

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
    public static void start() {
        INSTANCE = new InGameTimer();
        INSTANCE.category = SpeedRunOptions.getOption(SpeedRunOptions.TIMER_CATEGORY);
        INSTANCE.setPause(true, TimerStatus.IDLE);
    }

    /**
     * Start the Timer, Trigger when player to join(created) the world
     */
    public static void reset() {
        INSTANCE = new InGameTimer(false);
        INSTANCE.category = RunCategory.CUSTOM;
        INSTANCE.setPause(true, TimerStatus.IDLE);
        INSTANCE.setPause(false);
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
        InGameTimer timer = INSTANCE;
        if (timer.getStatus() == TimerStatus.COMPLETED) return;
        timer.endTime = System.currentTimeMillis();
        timer.endIGTTime = timer.endTime - timer.leastTickTime;

        timer.setStatus(TimerStatus.COMPLETED);
        timer.pauseLog.append("Result > IGT ").append(timeToStringFormat(timer.getInGameTime()))
                .append(", R-RTA ").append(timeToStringFormat(timer.getRealTimeAttack()))
                .append(", RTA ").append(timeToStringFormat(timer.getRealTimeAttack(false)))
                .append(", Counted Ticks: ").append(timer.activateTicks)
                .append(", Total Ticks: ").append(timer.loggerTicks)
                .append(", Rebased RTA Time: ").append(timeToStringFormat(timer.rebaseRealTime))
                .append(", Rebased IGT Time: ").append(timeToStringFormat(timer.rebaseIGTime));
        if (timer.category == RunCategory.ALL_ADVANCEMENTS)
            timer.pauseLog.append(", Excluded RTA Time: ").append(timeToStringFormat(timer.excludedTime));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        String logInfo = "MC Version : " + SharedConstants.getGameVersion().getName() + "\r\n"
                + "Timer Version : " + SpeedRunIGT.MOD_VERSION + "\r\n"
                + "Run Date : " + simpleDateFormat.format(new Date()) + "\r\n"
                + "====================\r\n";

        new Thread(() -> {
            try {
                FileUtils.writeStringToFile(new File(SpeedRunIGT.WORLDS_PATH.resolve(currentWorldName).toFile(), "igt_timer.log"), logInfo + timer.firstInput + "\r\n" + timer.pauseLog, Charsets.UTF_8);
                FileUtils.writeStringToFile(new File(SpeedRunIGT.WORLDS_PATH.resolve(currentWorldName).toFile(), "igt_freeze.log"), logInfo + timer.freezeLog, Charsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        for (Consumer<InGameTimer> onCompleteConsumer : onCompleteConsumers) {
            try {
                onCompleteConsumer.accept(INSTANCE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void leave() {
        if (INSTANCE.getStatus() == TimerStatus.COMPLETED) return;

        INSTANCE.leaveTime = System.currentTimeMillis();
        INSTANCE.setPause(true, TimerStatus.IDLE);

        String data = SpeedRunIGT.GSON.toJson(INSTANCE);
        String timerData = Crypto.encrypt(data, "faRQOs2GK5j863eP");
        try {
            FileUtils.writeStringToFile(new File(SpeedRunIGT.TIMER_PATH.toFile(), currentWorldName+".igt"), timerData, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean load(String name) {
        File file = new File(SpeedRunIGT.TIMER_PATH.toFile(), name+".igt");
        if (file.exists()) {
            try {
                String data = Crypto.decrypt(FileUtils.readFileToString(file, StandardCharsets.UTF_8), "faRQOs2GK5j863eP");
                INSTANCE = SpeedRunIGT.GSON.fromJson(data, InGameTimer.class);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }



    public @NotNull RunCategory getCategory() {
        return category;
    }

    public void setCategory(RunCategory category) {
        this.category = category;
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
        if (this.getStatus() == TimerStatus.COMPLETED && status != TimerStatus.NONE) return;
        this.status = status;
    }

    public boolean isStarted() {
        return this.startTime != 0;
    }

    public boolean isPaused() {
        return this.getStatus() == TimerStatus.PAUSED || this.getStatus() == TimerStatus.IDLE;
    }

    public boolean isPlaying() {
        return !this.isPaused() && this.getStatus() != TimerStatus.COMPLETED;
    }

    private long getEndTime() {
        return this.getStatus() == TimerStatus.COMPLETED ? this.endTime : System.currentTimeMillis();
    }

    public long getStartTime() {
        return this.isStarted() ? startTime : System.currentTimeMillis();
    }

    public long getRealTimeAttack() {
        return getRealTimeAttack(true);
    }

    public long getRealTimeAttack(boolean withRebased) {
        return this.getStatus() == TimerStatus.NONE ? 0 : this.getEndTime() - this.getStartTime() + (withRebased ? rebaseRealTime : 0) - this.excludedTime;
    }

    public long getInGameTime() { return getInGameTime(true); }

    public long getInGameTime(boolean smooth) {
        long ms = System.currentTimeMillis();
        return !isStarted() ? 0 :
                (this.getTicks() * 50L) // Tick Based
                        + Math.min(50, smooth && isPlaying() && this.leastTickTime != 0 ? ms - this.leastTickTime : 0) // More smooth timer in playing
                        - this.rebaseIGTime // Subtract Rebased time
                        + this.endIGTTime;
    }

    private long firstRenderedTime = 0;
    public void updateFirstInput() {
        if (firstInput.isEmpty() && !SpeedRunOptions.getOption(SpeedRunOptions.WAITING_FIRST_INPUT)) {
            firstInput = "First Input: IGT " + timeToStringFormat(getInGameTime(false)) + (leastTickTime == 0 ? "" : " (+ " + (System.currentTimeMillis() - this.leastTickTime) + "ms)") + ", RTA " + timeToStringFormat(getRealTimeAttack());
        }
        if (firstRenderedTime != 0) {
            firstInput = "First World Rendered: " + (System.currentTimeMillis() - this.firstRenderedTime) + "ms before first input";
        }
    }

    public void updateFirstRendered() {
        if (firstInput.isEmpty() && firstRenderedTime == 0 && SpeedRunOptions.getOption(SpeedRunOptions.WAITING_FIRST_INPUT)) {
            firstRenderedTime = System.currentTimeMillis();
        }
    }


    public void tick() {
        if (this.getStatus() == TimerStatus.COMPLETED) return;

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
        if (tickDelays != currentTime && Math.abs(50 - tickDelays) > 1) {
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
    }

    public void setPause(boolean isPause) { this.setPause(isPause, TimerStatus.PAUSED); }
    public void setPause(boolean toPause, TimerStatus toStatus) {
        if (this.getStatus() == TimerStatus.COMPLETED) return;

        if (toPause) {
            if (this.getStatus().getPause() <= toStatus.getPause()) {
                if (this.getStatus().getPause() < 1 && this.isStarted()) {
                    loggerPausedTime = getRealTimeAttack();
                    pauseLog.append(timeToStringFormat(getInGameTime())).append(" IGT, ").append(timeToStringFormat(loggerPausedTime)).append(" RTA S, ");
                }
                this.setStatus(toStatus);
            }
        } else {
            if (this.isStarted()) {
                if (isPaused()) {
                    long nowTime = getRealTimeAttack();
                    pauseLog.append(timeToStringFormat(nowTime)).append(" RTA E, ").append(timeToStringFormat(nowTime - loggerPausedTime)).append(" Length (").append(leaveTime != 0 ? TimerStatus.LEAVE_LEGACY.getMessage() : getStatus().getMessage()).append(")\r\n");
                    if (category == RunCategory.ALL_ADVANCEMENTS && leaveTime != 0) excludedTime = System.currentTimeMillis() - leaveTime;
                    leaveTime = 0;
                }
                if (this.getStatus() == TimerStatus.IDLE && loggerTicks != 0) {
                    leastStartTime = System.currentTimeMillis();
                }
            } else {
                startTime = System.currentTimeMillis();
                if (loggerTicks != 0) leastStartTime = startTime;
            }
            this.setStatus(TimerStatus.RUNNING);
        }
    }

    public boolean isResettable() {
        return isResettable;
    }
}
