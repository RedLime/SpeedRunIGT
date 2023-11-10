package com.redlimerl.speedrunigt.instance;

import com.redlimerl.speedrunigt.events.EventRepository;

import java.nio.file.Path;

public class TimerWorld {
    private static final String EVENT_LOG_FILE_NAME = "events.log";
    public final EventRepository eventRepository;

    TimerWorld(Path worldFolderPath, Path globalLogPath) {
        Path eventLogPath = worldFolderPath.resolve(EVENT_LOG_FILE_NAME);
        this.eventRepository = new EventRepository(eventLogPath, globalLogPath);
    }
}