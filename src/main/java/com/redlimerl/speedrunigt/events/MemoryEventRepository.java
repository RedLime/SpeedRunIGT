package com.redlimerl.speedrunigt.events;

import com.minecraftspeedrunning.srigt.common.events.Event;

import java.util.*;

public class MemoryEventRepository implements EventRepository {
    private final List<Event> events;

    public MemoryEventRepository() {
        events = new ArrayList<>();
    }
    public MemoryEventRepository(List<Event> events) {
        this.events = events;
    }

    @Override
    public void addEvent(Event event) {
        events.add(event);
    }

    @Override
    public void addEvents(Collection<Event> events) {
        this.events.addAll(events);
    }

    @Override
    public List<Event> getEvents() {
        return events;
    }
}
