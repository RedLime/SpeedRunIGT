package com.redlimerl.speedrunigt.events;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EventFactory {
    Integer eventVersion;
    String name;
    String eventId;
    String type;
    @Nullable String dataString;
    final Map<String, String> data;

    public EventFactory(String source, String name, String type, @Nullable String data) {
        this.eventVersion = 0;
        this.name = name;
        this.eventId = (source + "." + name).replace(" ", "_");
        this.type = type;
        this.dataString = data;
        this.data = Event.decodeDataString(data);
    }

    public String getDataValue(String key) {
        return this.data.get(key);
    }

    public Event create() {
        InGameTimer timer = InGameTimer.getInstance();
        return this.create(timer.getRetimedInGameTime(), timer.getRealTimeAttack());
    }

    private Event create(@NotNull Long gameTime, @NotNull Long realTime) {
        return new Event(this.eventVersion, this.eventId, this.type, gameTime, realTime);
    }
}
