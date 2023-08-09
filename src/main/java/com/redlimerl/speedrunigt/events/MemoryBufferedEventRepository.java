package com.redlimerl.speedrunigt.events;

import com.minecraftspeedrunning.srigt.common.events.Event;

import java.util.*;

public class MemoryBufferedEventRepository extends BufferedEventRepository {
    private final MemoryEventRepository memoryEventRepository;

    public MemoryBufferedEventRepository(EventRepository eventRepository) {
        super(eventRepository);
        this.memoryEventRepository = new MemoryEventRepository(eventRepository.getEvents());
    }

    @Override
    public List<Event> getEvents() {
        return memoryEventRepository.getEvents();
    }

    @Override
    public void add(Event event) {
        super.add(event);
        memoryEventRepository.add(event);
    }

    @Override
    public void addAll(Collection<Event> events) {
        super.addAll(events);
        memoryEventRepository.addAll(events);
    }
}
