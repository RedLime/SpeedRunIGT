package com.redlimerl.speedrunigt.timer.logs;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;

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
    private final long n;


    public TimerPauseLog(String pauseReason, String unpauseReason, long currentIGT, long currentRTA, long pauseLength, long pauseCount) {
        this.rp = pauseReason;
        this.ru = unpauseReason;
        this.igt = currentIGT;
        this.ct = currentRTA;
        this.pt = pauseLength;
        this.n = pauseCount;
    }

    @Override
    public String toString() {
        return String.format("#%s) IGT %s, RTA %s-%s (%s ms), Paused by %s, Unpause by %s",
                n, InGameTimerUtils.timeToStringFormat(igt), InGameTimerUtils.timeToStringFormat(ct - pt),
                InGameTimerUtils.timeToStringFormat(ct), pt, rp, ru);
    }
}
