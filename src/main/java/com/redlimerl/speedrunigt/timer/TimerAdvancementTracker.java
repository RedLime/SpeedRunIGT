package com.redlimerl.speedrunigt.timer;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TimerAdvancementTracker implements Serializable {
    @SuppressWarnings("unused")
    public static class Track implements Serializable {
        private long igt;
        private long rta;

        public Track(long igt, long rta) {
            this.igt = igt;
            this.rta = rta;
        }

        public void setTime(long igt, long rta) {
            this.igt = igt;
            this.rta = rta;
        }

        public long getRTA() {
            return rta;
        }

        public long getIGT() {
            return igt;
        }
    }
    public static class AdvancementTrack extends Track {
        private boolean complete;
        private boolean is_advancement;
        private final LinkedHashMap<String, Track> criteria = Maps.newLinkedHashMap();

        public AdvancementTrack() {
            super(0, 0);
            this.is_advancement = false;
            this.complete = false;
        }

        public void addCriteria(String string, long igt, long rta) {
            if (criteria.containsKey(string)) return;
            criteria.put(string, new Track(igt, rta));
        }

        public boolean isCompletedCriteria(String string) {
            return criteria.containsKey(string);
        }

        public void setComplete(boolean b) {
            this.complete = b;
        }

        public boolean isComplete() { return complete; }

        public void setAdvancement(boolean is_advancement) {
            this.is_advancement = is_advancement;
        }

        public boolean isAdvancement() {
            return is_advancement;
        }
    }

    private final ConcurrentMap<String, AdvancementTrack> advancements = Maps.newConcurrentMap();

    public AdvancementTrack getOrCreateTrack(String string) {
        if (advancements.containsKey(string)) return advancements.get(string);
        AdvancementTrack track = new AdvancementTrack();
        advancements.put(string, track);
        return track;
    }

    public Map<String, AdvancementTrack> getAdvancements() {
        return advancements;
    }
}
