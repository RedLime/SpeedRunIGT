package com.redlimerl.speedrunigt.instance;

import com.minecraftspeedrunning.srigt.common.events.Event;
import com.redlimerl.speedrunigt.events.BufferedEventRepository;
import com.redlimerl.speedrunigt.events.FileEventRepository;
import com.redlimerl.speedrunigt.events.MemoryBufferedEventRepository;
import com.redlimerl.speedrunigt.events.EventRepository;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Instance {
    private static final String EVENT_LOG_FILE_NAME = "events.log";
    public static final ExecutorService saveManagerThread = Executors.newSingleThreadExecutor();
    private World world;
    private TimerRelationship timerRelationship;
    private HostMode hostMode;
    private BufferedEventRepository instanceEventRepository;
    private EventRepository networkEventRepository;

    Instance(Path instancePath) {
        Path eventLogPath = instancePath.resolve(EVENT_LOG_FILE_NAME);
        EventRepository fileEventRepository = new FileEventRepository(eventLogPath);
        instanceEventRepository = new MemoryBufferedEventRepository(fileEventRepository);
    }

    void addNetworkEvents(List<Event> events) {
        if (timerRelationship != TimerRelationship.FOLLOWER) {
            return;
        }
        networkEventRepository.addAll(events);
    }

    void addEvent(Event event) {
        if (timerRelationship != TimerRelationship.LEADER) {
            return;
        }
        instanceEventRepository.add(event);
        world.eventRepository.add(event);
    }

    void flush() {
        if (timerRelationship != TimerRelationship.LEADER) {
            return;
        }
        if (hostMode == HostMode.MULTIPLAYER) {
            List<Event> events = world.eventRepository.getQueue();
            // TODO: push events to followers
        }
        world.eventRepository.flush();
    }

    List<Event> getEvents() {
        if (timerRelationship == TimerRelationship.FOLLOWER) {
            return networkEventRepository.getEvents();
        }
        if (timerRelationship == TimerRelationship.LEADER) {
            return world.eventRepository.getEvents();
        }
        return null;
    }
}
