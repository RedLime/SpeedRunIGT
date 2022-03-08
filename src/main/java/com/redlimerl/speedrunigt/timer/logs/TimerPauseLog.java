package com.redlimerl.speedrunigt.timer.logs;

public class TimerPauseLog {

    // Pause reason
    private final String rp;
    // Unpause reason
    private final String ru;
    // Current IGT
    private final long igt;
    // Current RTA
    private final long ct;
    // Pause length
    private final long pt;
    // Pause count
    private final int n;
    // retimed amount
    private final long r;


    public TimerPauseLog(String pauseReason, String unpauseReason, long currentIGT, long currentRTA, long pauseLength, int pauseCount, long retimed) {
        this.rp = pauseReason;
        this.ru = unpauseReason;
        this.igt = currentIGT;
        this.ct = currentRTA;
        this.pt = pauseLength;
        this.n = pauseCount;
        this.r = retimed;
    }

    public String getPauseReason() {
        return rp;
    }

    public String getUnpauseReason() {
        return ru;
    }

    public long getIGT() {
        return igt;
    }

    public long getUnpauseRTA() {
        return ct;
    }

    public long getPauseLength() {
        return pt;
    }

    public int getPauseCount() {
        return n;
    }

    public long getRetimeNeedAmount() {
        return r;
    }
}
