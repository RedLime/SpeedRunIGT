package com.redlimerl.speedrunigt.events;

import java.util.*;

public class BufferedEventRepository implements EventRepository {
    private final Queue<Event> eventQueue = new ArrayDeque<>();
    private final EventRepository eventRepository;
    private final List<Event> events;

    public BufferedEventRepository(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
        this.events = eventRepository.loadEvents();
    }

    /**
     * queue up events to effect timer adjustments
     * events queued up should only be events that the player could reasonably be aware of
     * @param event - the event that we want to start tracking at this point
     */
    public void appendEvent(Event event) {
        appendEvents(Collections.singletonList(event));
    }

    @Override
    public void appendEvents(Collection<Event> events) {
        this.events.addAll(events);
        this.eventQueue.addAll(events);
    }

    /**
     * write pending events to log file in separate thread
     */
    public void flushEvents() {
        if (this.eventQueue.isEmpty()) {
            return;
        }

        this.eventRepository.appendEvents(this.eventQueue);
        this.eventQueue.clear();
    }

    /**
     * @return all events that have happened so far in world
     */
    public List<Event> loadEvents() {
        return events;
    }
}
