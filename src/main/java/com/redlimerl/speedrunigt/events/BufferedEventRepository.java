package com.redlimerl.speedrunigt.events;

import com.minecraftspeedrunning.srigt.common.events.Event;

import java.util.*;

public class BufferedEventRepository implements EventRepository {
    private final Queue<Event> eventQueue = new ArrayDeque<>();
    private final EventRepository eventRepository;
    private final MemoryEventRepository memoryEventRepository;

    public BufferedEventRepository(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
        this.memoryEventRepository = new MemoryEventRepository(eventRepository.getEvents());
    }

    public void flushEvents() {
        if (this.eventQueue.isEmpty()) {
            return;
        }

        this.eventRepository.addEvents(this.eventQueue);
        this.eventQueue.clear();
    }

    @Override
    public void addEvent(Event event) {
        eventRepository.addEvent(event);
        memoryEventRepository.addEvent(event);
    }

    @Override
    public void addEvents(Collection<Event> events) {
        eventRepository.addEvents(events);
        memoryEventRepository.addEvents(events);
    }

    @Override
    public List<Event> getEvents() {
        return memoryEventRepository.getEvents();
    }
}
