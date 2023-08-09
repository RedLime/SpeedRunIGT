package com.redlimerl.speedrunigt.events;

import java.util.Collection;
import java.util.List;

public interface EventRepository {
    List<Event> loadEvents();
    void appendEvents(Collection<Event> eventQueue);
}
