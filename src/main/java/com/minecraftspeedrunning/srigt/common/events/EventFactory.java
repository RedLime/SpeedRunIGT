package com.minecraftspeedrunning.srigt.common.events;

import org.jetbrains.annotations.NotNull;

public class EventFactory {
    String eventId;
    Integer eventVersion;

    public EventFactory(String source, String name) {
        this(source, name, 0);
    }

    public EventFactory(String source, String name, Integer eventVersion) {
        // TODO: escape spaces in the source and name
        eventId = source + "." + name;
        this.eventVersion = eventVersion;
    }

    public Event create(@NotNull Long gameTime, @NotNull Long realTime) {
        return new Event(eventVersion, eventId, realTime, gameTime);
    }

    public Event create(@NotNull Long gameTime, @NotNull Long realTime, String data) {
        return new Event(eventVersion, eventId, realTime, gameTime, data);
    }
}
