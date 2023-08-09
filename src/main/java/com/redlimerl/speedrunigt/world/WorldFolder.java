package com.redlimerl.speedrunigt.world;


import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.events.Event;
import com.redlimerl.speedrunigt.events.EventRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldFolder implements EventRepository {
    private static final ExecutorService saveManagerThread = Executors.newSingleThreadExecutor();
    private static final String EVENT_LOG_FILE_NAME = "events.log";

    private final Path worldFolderPath;
    private final Map<String, Integer> eventVersions = new HashMap<>();

    WorldFolder(Path worldFolderPath) {
        this.worldFolderPath = worldFolderPath;

        Path worldTimerFolderPath = getTimerFolder();

        if (Files.notExists(worldFolderPath) || !Files.isDirectory(worldFolderPath)) {
            SpeedRunIGT.error("World directory doesn't exist, couldn't make timer dirs");
        }

        if (Files.notExists(worldFolderPath.resolve(SpeedRunIGT.MOD_ID))) {
            // TODO: create timer folder and log
        }
        else if (!Files.isDirectory(worldTimerFolderPath)) {
            // TODO: throw error
        }
    }

    private Path getTimerFolder() {
        return worldFolderPath.resolve(SpeedRunIGT.MOD_ID);
    }

    private Path getEventsPath() {
        return getTimerFolder().resolve(EVENT_LOG_FILE_NAME);
    }

    private List<Event> loadFallbackEvents() {
        // TODO: log error saying we are using fallback event
        // TODO: copy to backup location
        return new ArrayList<>();
    }

    public List<Event> loadEvents() {
        Path eventsPath = getEventsPath();
        if (Files.notExists(eventsPath)) {
            return new ArrayList<>();
        }
        if (!Files.isRegularFile(eventsPath)) {
            return loadFallbackEvents();
        }
        try(Stream<String> eventStrings = Files.lines(eventsPath)) {
            return eventStrings
                    .map((eventString) -> {
                        Event event = Event.parse(eventString, eventVersions);
                        if (event != null) {
                            eventVersions.put(event.id, event.version);
                        }
                        return event;
                    })
                    .unordered()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Event::getRealTime))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return loadFallbackEvents();
        }
    }

    public void appendEvents(Collection<Event> eventQueue) {
        String newEvents = eventQueue.stream()
                .map((event) -> {
                    Boolean writeVersion = !event.version.equals(eventVersions.getOrDefault(event.id, 0));
                    if (writeVersion) {
                        eventVersions.put(event.id, event.version);
                    }
                    return event.serialize(writeVersion);
                })
                .collect(Collectors.joining("\n", "", "\n"));

        saveManagerThread.submit(() -> {
            try {
                Files.write(
                        getEventsPath(),
                        newEvents.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                // TODO: log error
            }
        });
    }
}