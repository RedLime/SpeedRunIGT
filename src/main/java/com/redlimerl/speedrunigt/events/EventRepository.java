package com.redlimerl.speedrunigt.events;

import com.redlimerl.speedrunigt.instance.GameInstance;
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
    private final Path eventsPath;
    private final Path globalEventsPath;

    public EventRepository(Path eventsPath, Path globalEventsPath) {
        this.eventsPath = eventsPath;
        this.globalEventsPath = globalEventsPath;
    }

    private List<Event> getOldEvents() {
        if (Files.notExists(this.eventsPath) || !Files.isRegularFile(this.eventsPath)) {
            LOGGER.info("Can't load old events.");
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

    private void appendEventsFile(Path path, String text) {
        GameInstance.SAVE_MANAGER_THREAD.submit(() -> {
            try {
                Files.write(
                        path,
                        text.getBytes(Charset.defaultCharset()),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
                LOGGER.info("Successfully appended to events file. (" + path.getFileName() + ")");
            } catch (IOException e) {
                LOGGER.error("Error while writing events file " + path.getFileName(), e);
            }
        });
    }

    public List<Event> appendOldGlobal() {
        List<Event> events = this.getOldEvents();
        if (!events.isEmpty()) {
            StringBuilder newEvents = new StringBuilder();
            for (Event event : events) {
                newEvents.append(this.serializeEvent(event)).append("\n");
            }
            this.appendEventsFile(this.globalEventsPath, newEvents.toString());
        }
        return events;
    }

    public void add(Event event) {
        this.appendToFiles(this.serializeEvent(event) + "\n");
    }

    private void appendToFiles(String string) {
        this.appendEventsFile(this.eventsPath, string);
        this.appendEventsFile(this.globalEventsPath, string);
    }
}
