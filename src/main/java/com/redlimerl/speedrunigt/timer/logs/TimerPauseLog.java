package com.redlimerl.speedrunigt.timer.logs;

import java.io.Serializable;

public class TimerPauseLog implements Serializable {

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
    private final Retime r;


    public TimerPauseLog(String pauseReason, String unpauseReason, long currentIGT, long currentRTA, long pauseLength, int pauseCount, Retime retime) {
        this.rp = pauseReason;
        this.ru = unpauseReason;
        this.igt = currentIGT;
        this.ct = currentRTA;
        this.pt = pauseLength;
        this.n = pauseCount;
        this.r = retime;
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

    public Retime getRetimeData() {
        return r;
    }

    public static class Retime implements Serializable {
        private final long i;
        private final String t;
        public Retime(long retimed, String notice) {
            this.i = retimed;
            this.t = notice;
        }

        public long getRetimeNeedAmount() {
            return i;
        }

        public String getNoticeInfo() {
            return t;
        }
    }
}
