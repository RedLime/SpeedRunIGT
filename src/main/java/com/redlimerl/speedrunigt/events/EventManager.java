package com.redlimerl.speedrunigt.events;

import com.minecraftspeedrunning.srigt.common.events.Event;

import java.util.List;

public interface EventManager {
    void addEvent(Event event);
    List<Event> getEvents();
}
