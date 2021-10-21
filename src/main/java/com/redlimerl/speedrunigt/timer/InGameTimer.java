package com.redlimerl.speedrunigt.timer;

import org.jetbrains.annotations.NotNull;

/**
 * In-game Timer class.
 * {@link TimerStatus}
 */
public class InGameTimer {

    @NotNull
    public static InGameTimer INSTANCE = new InGameTimer();

    private long startTime = 0;
    private long endTime = 0;
    private long pauseTime = 0;
    private long pauseStartTime = 0;

    @NotNull
    private TimerStatus status = TimerStatus.NONE;

    /**
     * Start the Timer, Trigger when player to join(created) the world
     */
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.pauseTime = 0;
        this.endTime = 0;
        this.setPause(true, TimerStatus.IDLE);
    }

    /**
     * End the Timer, Trigger when player leave
     */
    public void end() {
        pauseStartTime = 0;
        this.setStatus(TimerStatus.NONE);
    }


    /**
     * End the Timer, Trigger when Complete Ender Dragon
     */
    public void complete() {
        pauseStartTime = 0;
        this.endTime = System.currentTimeMillis();
        this.setStatus(TimerStatus.COMPLETED);
    }

    public @NotNull TimerStatus getStatus() {
        return status;
    }

    public void setStatus(@NotNull TimerStatus status) {
        if (this.getStatus() == TimerStatus.COMPLETED) return;
        this.status = status;
    }

    public void setPause(boolean isPause) { this.setPause(isPause, TimerStatus.PAUSED); }
    public void setPause(boolean isPause, TimerStatus toPause) {
        if (this.getStatus() == TimerStatus.COMPLETED) return;

        if (isPause) {
            if (!this.isPause()) {
                this.pauseStartTime = System.currentTimeMillis();
            }
            if (!(this.getStatus() == TimerStatus.IDLE && toPause == TimerStatus.PAUSED)) {
                this.setStatus(toPause);
            }
        } else {
            if (this.isPause()) {
                this.pauseTime += System.currentTimeMillis() - this.pauseStartTime;
            }
            this.pauseStartTime = 0;
            this.setStatus(TimerStatus.RUNNING);
        }
    }

    public boolean isPause() {
        return this.pauseStartTime != 0;
    }

    private long getEndTime() {
        return this.getStatus() == TimerStatus.COMPLETED ? this.endTime : System.currentTimeMillis();
    }

    public long getRealTimeAttack() {
        return this.getStatus() == TimerStatus.NONE ? 0 : this.getEndTime() - this.startTime;
    }

    public long getInGameTime() {
        return this.getStatus() == TimerStatus.NONE ? 0 : this.getEndTime() - this.startTime - this.pauseTime
                - (this.isPause() ? System.currentTimeMillis() - pauseStartTime : 0);
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
