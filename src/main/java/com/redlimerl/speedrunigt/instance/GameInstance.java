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
    private final List<Event> bufferedEvents;
    private List<Event> events;
    private final Path globalEventsPath;
    private TimerWorld world;

    private GameInstance() {
        this.bufferedEvents = new ArrayList<>();
        this.globalEventsPath = SpeedRunIGT.getGlobalPath().resolve("latest_world.json");
    }

    public static GameInstance getInstance() {
        return INSTANCE;
    }

    public static void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameInstance();
        }
    }

    public void tryLoadWorld(String worldName) {
        File worldFile = InGameTimerUtils.getTimerLogDir(worldName, "");
        if (worldFile != null) {
            this.loadWorld(worldFile.toPath());
            LOGGER.info("Loaded events world.");
        } else {
            LOGGER.error("Didn't load events world.");
        }
    }

    public void ensureWorld() {
        if (!this.hasWorldLoaded()) {
            InGameTimer timer = InGameTimer.getInstance();
            LOGGER.info("Attempting event world load at " + timer.getWorldName());
            this.tryLoadWorld(timer.getWorldName());
        }
    }

    private void loadWorld(Path worldTimerDir) {
        this.world = new TimerWorld(worldTimerDir, this.globalEventsPath);
        this.events = this.world.getEventRepository().getOldEvents();
        this.addBufferedEvents();
    }

    public void closeTimer() {
        this.bufferedEvents.clear();
        this.events = null;
        this.world = null;
    }

    private void addBufferedEvents() {
        if (this.world != null && !this.bufferedEvents.isEmpty()) {
            List<Event> buffered = new ArrayList<>(this.bufferedEvents);
            int removed = 0;
            for (Event bufferedEvent : buffered) {
                if (!this.hasTriggeredEvent(bufferedEvent)) {
                    if (this.events != null) {
                        this.events.add(bufferedEvent);
                    } else {
                        // Since the list of old events hasn't loaded yet, we can't know for sure if the event has been triggered or not, so we just add the buffered events later.
                        LOGGER.error("Couldn't add buffered event to events array.");
                        return;
                    }
                    this.world.getEventRepository().add(bufferedEvent);
                    this.bufferedEvents.remove(bufferedEvent);
                    removed++;
                }
            }
            LOGGER.info("Loaded " + removed + " buffered event" + (removed != 1 ? "s" : "") + ".");
        }
    }

    public void addEvent(Event event) {
        if (this.world != null && this.events != null) {
            if (!this.hasTriggeredEvent(event)) {
                this.addBufferedEvents();
                this.world.getEventRepository().add(event);
                this.events.add(event);
            }
        } else {
            this.bufferedEvents.add(event);
        }
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasTriggeredEvent(Event e) {
        if (this.events == null) {
            return false;
        }
        for (Event event : this.events) {
            if (event.id.equalsIgnoreCase(e.id)) {
                if (!event.repeatable) {
                    return true;
                }
                if (event.gameTime.equals(e.gameTime) && event.realTime.equals(e.realTime)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasWorldLoaded() {
        return this.world != null;
    }
}
