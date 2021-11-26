package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.crypt.Crypto;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    public static int renderedWorld = 0;

    private static final ArrayList<Consumer<InGameTimer>> onCompleteConsumers = new ArrayList<>();

    public static void onComplete(Consumer<InGameTimer> supplier) {
        onCompleteConsumers.add(supplier);
    }

    private RunCategory category = RunCategory.ANY;

    private long startTime = 0;
    private long rsgStartTime = 0;
    private long endTime = 0;
    private long firstInputDelays = 0;

    private int ticks = 0;
    private int pauseTicks = 0;
    private int pausePointTick = 0;

    private long lastTickTime = 0;
    private long lastPauseTime = 0;
    private TimerStatus lastPauseStatus = TimerStatus.NONE;
    private final StringBuilder pauseLog = new StringBuilder();

    @NotNull
    private TimerStatus status = TimerStatus.NONE;

    private final HashMap<Integer, Integer> moreData = new HashMap<>();

    /**
     * Start the Timer, Trigger when player to join(created) the world
     */
    public static void start() {
        INSTANCE = new InGameTimer();
        INSTANCE.category = SpeedRunOptions.getOption(SpeedRunOptions.TIMER_CATEGORY);
        INSTANCE.setPause(true, INSTANCE.getCategory() == RunCategory.CUSTOM ? TimerStatus.WAITING : TimerStatus.IDLE);
    }

    /**
     * End the Timer, Trigger when player leave
     */
    public void end() {
        this.setStatus(TimerStatus.NONE);
    }


    /**
     * End the Timer, Trigger when Complete Ender Dragon
     */
    public void complete() {
        if (this.getStatus() == TimerStatus.COMPLETED) return;

        this.endTime = System.currentTimeMillis();
        this.setStatus(TimerStatus.COMPLETED);
        for (Consumer<InGameTimer> onCompleteConsumer : onCompleteConsumers) {
            onCompleteConsumer.accept(this);
        }

        new Thread(() -> {
            try {
                FileUtils.writeStringToFile(new File(SpeedRunIGT.WORLDS_PATH.resolve(currentWorldName).toFile(), "igt_log.txt"), pauseLog.toString(), Charsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void leave() {
        if (this.getStatus() == TimerStatus.COMPLETED) return;
        this.setPause(true, TimerStatus.LEAVE);

        String data = SpeedRunIGT.GSON.toJson(this);
        String timerData = Crypto.encrypt(data, "faRQOs2GK5j863ePvCBe5SiZLypm4UOM");
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
                String data = Crypto.decrypt(FileUtils.readFileToString(file, StandardCharsets.UTF_8), "faRQOs2GK5j863ePvCBe5SiZLypm4UOM");
                INSTANCE = SpeedRunIGT.GSON.fromJson(data, InGameTimer.class);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public @NotNull RunCategory getCategory() {
        return category;
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

    public void setPause(boolean isPause) { this.setPause(isPause, TimerStatus.PAUSED); }
    public void setPause(boolean toPause, TimerStatus toStatus) {
        if (this.getStatus() == TimerStatus.COMPLETED) return;

        if (toPause) {
            if (this.getStatus().getPause() <= toStatus.getPause()) {
                if (this.getStatus().getPause() < 1) {
                    this.pausePointTick = ticks;
                    if (this.isStarted()) {
                        lastPauseTime = getRealTimeAttack();
                        lastPauseStatus = toStatus;
                        pauseLog.append(timeToStringFormat(getInGameTime())).append(" IGT, ").append(timeToStringFormat(lastPauseTime)).append(" RTA S, ");
                    }
                }
                this.setStatus(toStatus);
            }
        } else {
            if (isPaused() && this.isStarted()) {
                long nowTime = getRealTimeAttack();
                pauseLog.append(timeToStringFormat(nowTime)).append(" RTA E, ").append(timeToStringFormat(nowTime - lastPauseTime)).append(" Length (").append(lastPauseStatus.getMessage()).append(")\n");
            }

            //첫 입력 대기 시간 적용
            if (this.getStatus() == TimerStatus.IDLE && this.isStarted()) {
                this.pauseTicks += this.ticks - this.pausePointTick;
                this.firstInputDelays += this.pausePointTick == this.ticks ? 0 : System.currentTimeMillis() - this.lastTickTime;
            }

            //첫 입력 타이머 시작
            if (!this.isStarted()) {
                this.startTime = System.currentTimeMillis();
                this.pauseTicks = this.ticks;
                this.firstInputDelays += this.startTime - lastTickTime;
                if (this.isCanStartRSG() || this.status == TimerStatus.WAITING) this.rsgStartTime = this.startTime;
            }
            this.setStatus(TimerStatus.RUNNING);
        }
    }

    private boolean wasResetRSG = false;
    public void startRSGTime() {
        if (this.getStatus() == TimerStatus.IDLE && this.isCanStartRSG()) {
            this.rsgStartTime = System.currentTimeMillis();
        }
    }

    public void resetRSGTime() {
        if (wasResetRSG || ticks == 0) return;

        this.rsgStartTime = 0;
        wasResetRSG = true;
    }

    private boolean isCanStartRSG() {
        return this.rsgStartTime == 0 && this.status != TimerStatus.WAITING;
    }

    public boolean isStarted() {
        return this.startTime != 0 && this.status != TimerStatus.WAITING;
    }

    public boolean isPaused() {
        return this.getStatus() == TimerStatus.PAUSED || this.getStatus() == TimerStatus.IDLE || this.getStatus() == TimerStatus.LEAVE || this.getStatus() == TimerStatus.WAITING;
    }

    public boolean isPausedOrCompleted() {
        return this.isPaused() || this.getStatus() == TimerStatus.COMPLETED;
    }

    private long getEndTime() {
        return this.getStatus() == TimerStatus.COMPLETED ? this.endTime : System.currentTimeMillis();
    }

    public long getStartTime() {
        return this.rsgStartTime != 0 ? rsgStartTime : System.currentTimeMillis();
    }

    public long getRealTimeAttack() {
        return this.getStatus() == TimerStatus.NONE ? 0 : this.getEndTime() - this.getStartTime();
    }

    public int getTicks() {
        return !this.isStarted() ? 0 : this.ticks - this.pauseTicks - (this.isPaused() ? this.ticks - this.pausePointTick : 0);
    }

    public void tick() {
        if (this.getStatus() == TimerStatus.COMPLETED) return;
        this.ticks++;
        this.lastTickTime = System.currentTimeMillis();
    }

    public long getInGameTime() {
        long ms = System.currentTimeMillis();
        return this.getStatus() == TimerStatus.NONE ? 0 :
                        (this.getTicks() * 50L) // Tick Based
                        + (!isPausedOrCompleted() && this.pausePointTick != this.ticks ? ms - this.lastTickTime : 0) // More smooth timer in playing
                        - (this.firstInputDelays); // Subtract First Input Delays
    }

    public static String timeToStringFormat(long time) {
        int seconds = ((int) (time / 1000)) % 60;
        int minutes = ((int) (time / 1000)) / 60;
        if (minutes > 59) {
            int hours = minutes / 60;
            minutes = minutes % 60;
            return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, time % 1000);
        } else {
            return String.format("%02d:%02d.%03d", minutes, seconds, time % 1000);
        }
    }
}
