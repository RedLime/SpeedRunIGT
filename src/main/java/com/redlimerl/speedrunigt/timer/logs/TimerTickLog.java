package com.redlimerl.speedrunigt.timer.logs;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;

import java.io.Serializable;

public class TimerTickLog implements Serializable {
    // Tick Number
    private final long t;
    // Total Tick Number
    private final long tt;
    // Previous Tick RTA
    private final long td;
    // Current Tick RTA
    private final long ct;
    // IGT
    private final long igt;
    // Is cause world load after changed world
    private final boolean wl;

    public TimerTickLog(long ticks, long totalTicks, long currentRTA, long tickDelay, long currentIGT, boolean isIdle) {
        this.t = ticks;
        this.tt = totalTicks;
        this.td = tickDelay;
        this.ct = currentRTA;
        this.igt = currentIGT;
        this.wl = isIdle;
    }

    @Override
    public String toString() {
        return String.format("Tick %s(%s), Delay %s ms, IGT %s, RTA %s-%s"+(wl ? ", due world load" : ""),
                tt, t, td, InGameTimerUtils.timeToStringFormat(igt), InGameTimerUtils.timeToStringFormat(ct-td), InGameTimerUtils.timeToStringFormat(ct));
    }
}
