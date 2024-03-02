package com.redlimerl.speedrunigt.instance;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.events.Event;
import com.redlimerl.speedrunigt.events.EventFactory;
import com.redlimerl.speedrunigt.events.EventFactoryLoader;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

    private static UUID getLocalPlayerID() {
        return MinecraftClient.getInstance().getSession().getUuidOrNull();
    }

    public void tryLoadWorld(String worldName) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            return;
        }
        File worldFile = InGameTimerUtils.getTimerLogDir(worldName, "");
        if (worldFile != null) {
            this.loadWorld(worldFile.toPath());
            LOGGER.info("Loaded events world.");
            checkJoinEvents();
        } else {
            LOGGER.error("Didn't load events world.");
        }
    }

    private void checkJoinEvents() {
        // Rejoin
        if (this.events.stream().anyMatch(event -> event.type.equals("leave_world"))) {
            this.callEvents("rejoin_world");
        }

        // Multiplayer check
        if (this.events.stream().anyMatch(event -> event.type.equals("multiplayer"))) return;
        Set<UUID> previousPlayers = this.world.getPreviousPlayers();
        if (previousPlayers.size() > 1 || (previousPlayers.size() == 1 && previousPlayers.stream().noneMatch(uuid -> uuid.equals(getLocalPlayerID())))) {
            this.callEvents("multiplayer");
        }
    }

    public void ensureWorld() {
        if (SpeedRunIGT.IS_CLIENT_SIDE && !this.hasWorldLoaded()) {
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
                if (this.canTriggerEvent(bufferedEvent)) {
                    if (this.events != null) {
                        this.events.add(bufferedEvent);
                    } else {
                        // Since the list of old events hasn't loaded yet, we can't know for sure if the event has been triggered or not, so we just add the buffered events later.
                        LOGGER.error("Couldn't add buffered event to events array.");
                        return;
                    }
                    this.sendEventToRepository(bufferedEvent);
                    this.bufferedEvents.remove(bufferedEvent);
                    removed++;
                }
            }
            LOGGER.info("Loaded " + removed + " buffered event" + (removed != 1 ? "s" : "") + ".");
        }
    }

    public void addEvent(Event event) {
        if (this.world != null && this.events != null) {
            if (this.canTriggerEvent(event)) {
                this.addBufferedEvents();
                this.events.add(event);
                this.sendEventToRepository(event);
            }
        } else {
            this.bufferedEvents.add(event);
        }
    }

    public void callEvents(String type) {
        this.callEvents(type, null);
    }

    public void callEvents(String type, Function<EventFactory, Boolean> condition) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) return;
        for (EventFactory factory : EventFactoryLoader.getEventFactories(type)) {
            if (condition == null || condition.apply(factory)) {
                this.addEvent(factory.create());
            }
        }
    }

    public boolean canTriggerEvent(Event e) {
        if (this.events == null) {
            return true;
        }
        boolean repeatable = EventFactoryLoader.isEventRepeatable(e);
        for (Event event : this.events) {
            if (event.id.equalsIgnoreCase(e.id)) {
                if (!repeatable) {
                    return false;
                }
                if (event.gameTime.equals(e.gameTime) && event.realTime.equals(e.realTime)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasWorldLoaded() {
        return this.world != null;
    }

    private boolean shouldUpdateGlobal(Event event) {
        return this.events.size() > 1 || !event.type.equals("leave_world");
    }

    private void sendEventToRepository(Event event) {
        if (this.shouldUpdateGlobal(event)) {
            this.world.getEventRepository().add(event);
        } else {
            this.world.getEventRepository().addOnlyToLog(event);
        }
    }
}
