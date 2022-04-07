package com.redlimerl.speedrunigt.timer.logs;

import java.io.Serializable;

public class TimerTimeline implements Serializable {
    private final String name;
    private final long igt;
    private final long rta;

    public TimerTimeline(String name, long igt, long rta) {
        this.name = name;
        this.igt = igt;
        this.rta = rta;
    }

    public String getName() {
        return name;
    }

    public long getIGT() {
        return igt;
    }

    public long getRTA() {
        return rta;
    }
}
