package com.redlimerl.speedrunigt.events;

import com.minecraftspeedrunning.srigt.common.events.Event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BufferedEventRepository implements EventRepository {
    protected final List<Event> queue = new ArrayList<>();
    private final EventRepository eventRepository;

    public BufferedEventRepository(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void flush() {
        if (this.queue.isEmpty()) {
            return;
        }

        eventRepository.addAll(queue);
        queue.clear();
    }

    public List<Event> getQueue() {
        return queue;
    }

    @Override
    public List<Event> getEvents() {
        flush();
        return eventRepository.getEvents();
    }

    @Override
    public void add(Event event) {
        queue.add(event);
    }

    @Override
    public void addAll(Collection<Event> events) {
        this.queue.addAll(events);
    }
}
