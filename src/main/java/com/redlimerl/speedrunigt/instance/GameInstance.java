package com.redlimerl.speedrunigt.instance;

import com.minecraftspeedrunning.srigt.common.events.Event;
import com.redlimerl.speedrunigt.SpeedRunIGTConfig;
import com.redlimerl.speedrunigt.events.*;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameInstance {
    private static final String EVENT_LOG_FILE_NAME = "events.log";
    private static GameInstance gameInstance;

    public static GameInstance getInstance() {
        return gameInstance;
    }
    public static void createInstance(Path instancePath) {
        if (gameInstance == null) {
            gameInstance = new GameInstance(instancePath);
        }
    }

    public static final ExecutorService saveManagerThread = Executors.newSingleThreadExecutor();

    private TimerMode timerMode;

    private World world;
    private EventRepository networkEventRepository;

    private final BufferedEventRepository instanceEventRepository;

    private GameInstance(Path instancePath) {
        Path eventLogPath = instancePath.resolve(EVENT_LOG_FILE_NAME);
        EventRepository fileEventRepository = new FileEventRepository(eventLogPath);
        instanceEventRepository = new MemoryBufferedEventRepository(fileEventRepository);
    }

    /**
     * load world hosted on this machine
     * @param worldFolderPath - path of the folder the world is saved at
     */
    public void loadWorld(Path worldFolderPath) {
        boolean isDedicated = SpeedRunIGTConfig.getConfig().isDedicated;
        timerMode = isDedicated ? TimerMode.MULTIPLAYER_SERVER : TimerMode.SINGLE_PLAYER;
        world = new World(worldFolderPath);
    }

    public void openToLan() {
        timerMode = TimerMode.MULTIPLAYER_SERVER;
    }

    /**
     * connect to world hosted on another machine
     * @param events - list of events that have already taken place on leader machine
     */
    public void connect(List<Event> events) {
        timerMode = TimerMode.MULTIPLAYER_CLIENT;
        networkEventRepository = new MemoryEventRepository(events);
    }

    public void closeTimer() {
        timerMode = null;
        world = null;
        networkEventRepository = null;
    }

    public void addNetworkEvents(List<Event> events) {
        if (timerMode.timerHierarchy != TimerHierarchy.FOLLOWER) {
            return;
        }
        networkEventRepository.addAll(events);
    }

    public void addEvent(Event event) {
        if (timerMode.timerHierarchy != TimerHierarchy.LEADER) {
            return;
        }
        instanceEventRepository.add(event);
        world.eventRepository.add(event);
    }

    public void flush() {
        if (timerMode.timerHierarchy != TimerHierarchy.LEADER) {
            return;
        }
        if (timerMode.gameMode == GameMode.MULTIPLAYER) {
            List<Event> events = world.eventRepository.getQueue();
            if (!events.isEmpty()) {
                // TODO: push events to followers
            }
        }
        world.eventRepository.flush();
    }

    List<Event> getEvents() {
        if (timerMode.timerHierarchy == TimerHierarchy.FOLLOWER) {
            return networkEventRepository.getEvents();
        }
        if (timerMode.timerHierarchy == TimerHierarchy.LEADER) {
            return world.eventRepository.getEvents();
        }
        return null;
    }
}
