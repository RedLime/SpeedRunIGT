package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import org.jetbrains.annotations.NotNull;

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
    public static InGameTimer INSTANCE = new InGameTimer();
    private static final ArrayList<Consumer<InGameTimer>> onCompleteConsumers = new ArrayList<>();
    public static void onComplete(Consumer<InGameTimer> supplier) {
        onCompleteConsumers.add(supplier);
    }

    private boolean isStart = false;
    private long startTime = 0;
    private long endTime = 0;
    private long firstInputDelays = 0;
    private int ticks = 0;
    private int pauseTicks = 0;
    private int pausePointTick = 0;
    private long lastTickTime = 0;

    @NotNull
    private TimerStatus status = TimerStatus.NONE;

    private final HashMap<Integer, Integer> moreData = new HashMap<>();

    /**
     * Start the Timer, Trigger when player to join(created) the world
     */
    public void start() {
        this.isStart = false;
        this.startTime = 0;
        this.firstInputDelays = 0;
        this.endTime = 0;
        this.ticks = 0;
        this.pauseTicks = 0;
        this.moreData.clear();
        this.setPause(true, TimerStatus.IDLE);
    }

    /**
     * End the Timer, Trigger when player leave
     */
    public void end() {
        this.startTime = 0;
        this.firstInputDelays = 0;
        this.endTime = 0;
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
    }

    public @NotNull RunCategory getCategory() {
        return SpeedRunOptions.getOption(SpeedRunOptions.TIMER_CATEGORY);
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
            //IDLE 전환 후 PAUSE 전환 시도 시 무시
            if (!(this.getStatus() == TimerStatus.IDLE && toStatus == TimerStatus.PAUSED)) {
                this.setStatus(toStatus);
                this.pausePointTick = ticks;
            }
        } else {
            //첫 입력 대기 시간 적용
            if (this.getStatus() == TimerStatus.IDLE && this.isStart) {
                this.pauseTicks += this.ticks - this.pausePointTick;
                this.firstInputDelays += System.currentTimeMillis() - this.lastTickTime;
            }

            //첫 입력 타이머 시작
            if (!this.isStart) {
                this.isStart = true;
                this.startTime = System.currentTimeMillis();
                this.pauseTicks = this.ticks;
                this.firstInputDelays += this.startTime - lastTickTime;
            }
            this.setStatus(TimerStatus.RUNNING);
        }
    }

    public boolean isPaused() {
        return this.getStatus() == TimerStatus.PAUSED || this.getStatus() == TimerStatus.IDLE;
    }

    public boolean isPausedOrCompleted() {
        return this.isPaused() || this.getStatus() == TimerStatus.COMPLETED;
    }

    private long getEndTime() {
        return this.getStatus() == TimerStatus.COMPLETED ? this.endTime : System.currentTimeMillis();
    }

    public long getStartTime() {
        return this.isStart ? startTime : System.currentTimeMillis();
    }

    public long getRealTimeAttack() {
        return this.getStatus() == TimerStatus.NONE ? 0 : this.getEndTime() - this.getStartTime();
    }

    public int getTicks() {
        return !this.isStart ? 0 : this.ticks - this.pauseTicks - (this.isPaused() ? this.ticks - this.pausePointTick : 0);
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
