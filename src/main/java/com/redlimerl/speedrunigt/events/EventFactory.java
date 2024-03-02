package com.redlimerl.speedrunigt.events;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EventFactory {
    final int eventVersion;
    final String eventId;
    final String type;
    final boolean repeatable;
    final Map<String, String> data;

    public EventFactory(String source, String name, String type, boolean repeatable, @Nullable String data) {
        this.eventVersion = 0;
        this.eventId = (source + "." + name).replace(" ", "_");
        this.type = type;
        this.repeatable = repeatable;
        this.data = Event.decodeDataString(data);
    }

    public String getDataValue(String key) {
        return this.data.get(key);
    }

    public Event create() {
        InGameTimer timer = InGameTimer.getInstance();
        return this.create(timer.getRealTimeAttack(), timer.getRetimedInGameTime());
    }

    private Event create(@NotNull Long realTime, @NotNull Long gameTime) {
        return new Event(this.eventVersion, this.eventId, this.type, realTime, gameTime);
    }
}
