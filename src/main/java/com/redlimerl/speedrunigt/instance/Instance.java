package com.redlimerl.speedrunigt.instance;

import com.minecraftspeedrunning.srigt.common.events.Event;
import com.redlimerl.speedrunigt.events.*;
import net.minecraft.client.MinecraftClient;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Instance {
    private static final String EVENT_LOG_FILE_NAME = "events.log";
    public static final ExecutorService saveManagerThread = Executors.newSingleThreadExecutor();

    private TimerMode timerMode;

    private World world;
    private EventRepository networkEventRepository;

    private final BufferedEventRepository instanceEventRepository;

    Instance(Path instancePath) {
        Path eventLogPath = instancePath.resolve(EVENT_LOG_FILE_NAME);
        EventRepository fileEventRepository = new FileEventRepository(eventLogPath);
        instanceEventRepository = new MemoryBufferedEventRepository(fileEventRepository);
    }

    /**
     * load world hosted on this machine
     * @param worldFolderPath - path of the folder the world is saved at
     */
    void loadWorld(Path worldFolderPath) {
        // TODO: replace this with a better check that I can find at not 4am
        boolean isDedicated = MinecraftClient.getInstance().getServer().isDedicated();
        timerMode = isDedicated ? TimerMode.MULTIPLAYER_SERVER : TimerMode.SINGLE_PLAYER;
        world = new World(worldFolderPath);
    }

    void openLan() {
        timerMode = TimerMode.MULTIPLAYER_SERVER;
    }

    /**
     * connect to world hosted on another machine
     * @param events - list of events that have already taken place on leader machine
     */
    void connect(List<Event> events) {
        timerMode = TimerMode.MULTIPLAYER_CLIENT;
        networkEventRepository = new MemoryEventRepository(events);
    }

    void addNetworkEvents(List<Event> events) {
        if (timerMode.timerRelationship != TimerRelationship.FOLLOWER) {
            return;
        }
        networkEventRepository.addAll(events);
    }

    void addEvent(Event event) {
        if (timerMode.timerRelationship != TimerRelationship.LEADER) {
            return;
        }
        instanceEventRepository.add(event);
        world.eventRepository.add(event);
    }

    void flush() {
        if (timerMode.timerRelationship != TimerRelationship.LEADER) {
            return;
        }
        if (timerMode.gameMode == GameMode.MULTIPLAYER) {
            List<Event> events = world.eventRepository.getQueue();
            // TODO: push events to followers
        }
        world.eventRepository.flush();
    }

    List<Event> getEvents() {
        if (timerMode.timerRelationship == TimerRelationship.FOLLOWER) {
            return networkEventRepository.getEvents();
        }
        if (timerMode.timerRelationship == TimerRelationship.LEADER) {
            return world.eventRepository.getEvents();
        }
        return null;
    }
}
