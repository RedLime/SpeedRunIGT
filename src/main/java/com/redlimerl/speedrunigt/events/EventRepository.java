package com.redlimerl.speedrunigt.events;

import com.redlimerl.speedrunigt.instance.GameInstance;
import com.redlimerl.speedrunigt.instance.TimerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventRepository {
    private static final Logger LOGGER = LogManager.getLogger("Event Repository");
    private final Map<String, Integer> eventVersions = new HashMap<>();
    private final TimerWorld world;
    private final Path eventsPath;
    private final Path globalEventsPath;

    public EventRepository(TimerWorld world, Path eventsPath, Path globalEventsPath) {
        this.world = world;
        this.eventsPath = eventsPath;
        this.globalEventsPath = globalEventsPath;
    }

    public List<Event> getOldEvents() {
        if (Files.notExists(this.eventsPath) || !Files.isRegularFile(this.eventsPath)) {
            LOGGER.info("Couldn't load old events.");
            return new ArrayList<>();
        }
        try (Stream<String> eventStrings = Files.lines(this.eventsPath)) {
            return eventStrings
                    .map((eventString) -> {
                        Event event = Event.parse(eventString, this.eventVersions);
                        if (event != null) {
                            this.eventVersions.put(event.id, event.version);
                        }
                        return event;
                    })
                    .unordered()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Event::getRealTime))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error while loading old events", e);
            return new ArrayList<>();
        }
    }

    private String serializeEvent(Event event) {
        boolean writeVersion = !event.version.equals(this.eventVersions.getOrDefault(event.id, 0));
        if (writeVersion) {
            this.eventVersions.put(event.id, event.version);
        }
        return event.serialize(writeVersion);
    }

    public void add(Event event) {
        GameInstance.SAVE_MANAGER_THREAD.submit(() -> {
            try {
                writeEventToLog(event);
                writeWorldDataToGlobalFile();
                LOGGER.info("Successfully appended to events files.");
            } catch (IOException e) {
                LOGGER.error("Error while writing to events files", e);
            }
        });
    }

    public void addOnlyToLog(Event event) {
        GameInstance.SAVE_MANAGER_THREAD.submit(() -> {
            try {
                writeEventToLog(event);
                LOGGER.info("Successfully appended to events file.");
            } catch (IOException e) {
                LOGGER.error("Error while writing to events files", e);
            }
        });
    }

    private void writeEventToLog(Event event) throws IOException {
        Files.write(
                this.eventsPath,
                (this.serializeEvent(event) + "\n").getBytes(Charset.defaultCharset()),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    private void writeWorldDataToGlobalFile() throws IOException {
        Files.write(
                this.globalEventsPath,
                (this.world.getWorldData() + "\n").getBytes(Charset.defaultCharset()),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
