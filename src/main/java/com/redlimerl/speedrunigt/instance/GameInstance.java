package com.redlimerl.speedrunigt.instance;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.events.Event;
import com.redlimerl.speedrunigt.events.EventFactory;
import com.redlimerl.speedrunigt.events.EventFactoryLoader;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class GameInstance {
    public static final ExecutorService SAVE_MANAGER_THREAD = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = LogManager.getLogger("Game Instance");
    private static GameInstance INSTANCE;
    private final List<Event> bufferedEvents = new ArrayList<>();
    private final List<Event> events = new ArrayList<>();
    private final Path globalEventsPath;
    private TimerWorld world;

    private GameInstance() {
        this.globalEventsPath = SpeedRunIGT.getGlobalPath().resolve("events.latest");
    }

    public static GameInstance getInstance() {
        return INSTANCE;
    }

    public static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameInstance();
        }
    }

    public void preWorldLoad() {
        this.bufferedEvents.clear();
        this.events.clear();
    }

    public void tryLoadWorld(String worldName) {
        File worldFile = InGameTimerUtils.getTimerLogDir(worldName, "");
        if (worldFile != null) {
            this.loadWorld(worldFile.toPath());
            SpeedRunIGT.debug("Loaded events world.");
        } else { SpeedRunIGT.error("Didn't load events world."); }
    }

    public void ensureWorld() {
        if (!this.hasWorldLoaded()) {
            InGameTimer timer = InGameTimer.getInstance();
            SpeedRunIGT.debug("Attempting event world load at " + timer.getWorldName());
            this.tryLoadWorld(timer.getWorldName());
        }
    }

    private void loadWorld(Path worldTimerDir) {
        this.clearGlobalPath();
        this.world = new TimerWorld(worldTimerDir, this.globalEventsPath);
        this.events.addAll(this.world.eventRepository.appendOldGlobal());
        this.addBufferedEvents();
    }

    public boolean hasWorldLoaded() {
        return this.world != null;
    }

    public void closeTimer() {
        this.world = null;
    }

    private void addBufferedEvents() {
        if (this.world != null && !this.bufferedEvents.isEmpty()) {
            for (Event bufferedEvent : this.bufferedEvents) {
                this.world.eventRepository.add(bufferedEvent);
            }
            SpeedRunIGT.debug("Loaded " + this.bufferedEvents.size() + " buffered event" + (this.bufferedEvents.size() != 1 ? "s" : "") + ".");
            this.bufferedEvents.clear();
        }
    }

    public void addEvent(Event event) {
        if (this.hasTriggeredEvent(event)) { return; }
        if (this.world != null) {
            this.addBufferedEvents();
            this.world.eventRepository.add(event);
        } else {
            this.bufferedEvents.add(event);
        }
        this.events.add(event);
    }

    private void clearGlobalPath() {
        SAVE_MANAGER_THREAD.submit(() -> {
            if (Files.exists(this.globalEventsPath)) {
                try {
                    Files.write(this.globalEventsPath, "".getBytes(StandardCharsets.UTF_8));
                    LOGGER.info("Successfully cleared global file.");
                } catch (IOException e) {
                    LOGGER.error("Error while clearing global file", e);
                }
            }
        });
    }

    public void callEvents(String type) {
        this.callEvents(type, null);
    }

    public void callEvents(String type, Function<EventFactory, Boolean> condition) {
        for (EventFactory factory : EventFactoryLoader.getEventFactories(type)) {
            if (condition == null || condition.apply(factory)) {
                this.addEvent(factory.create());
            }
        }
    }

    public boolean hasTriggeredEvent(Event e) {
        for (Event event : this.events) {
            if (event.type.equalsIgnoreCase(e.type)) {
                return true;
            }
        }
        return false;
    }
}
