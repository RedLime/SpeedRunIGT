package com.redlimerl.speedrunigt.events;


import com.minecraftspeedrunning.srigt.common.events.Event;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.redlimerl.speedrunigt.instance.Instance.saveManagerThread;

public class FileEventRepository implements EventRepository {
    private final Map<String, Integer> eventVersions = new HashMap<>();
    private final Path eventsPath;

    public FileEventRepository(Path eventsPath) {
        this.eventsPath = eventsPath;
    }

    private List<Event> loadFallbackEvents() {
        // TODO: log error saying we are using fallback event
        // TODO: copy to backup location
        return new ArrayList<>();
    }

    @Override
    public List<Event> getEvents() {
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

    private String serializeEvent(Event event) {
        Boolean writeVersion = !event.version.equals(eventVersions.getOrDefault(event.id, 0));
        if (writeVersion) {
            eventVersions.put(event.id, event.version);
        }
        return event.serialize(writeVersion);
    }

    private void appendEventsFile(String text) {
        saveManagerThread.submit(() -> {
            try {
                Files.write(
                        eventsPath,
                        text.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            } catch (IOException e) {
                // TODO: log error
            }
        });
    }

    @Override
    public void add(Event event) {
        String newEvents = this.serializeEvent(event) + "\n";
        appendEventsFile(newEvents);
    }

    @Override
    public void addAll(Collection<Event> eventQueue) {
        String newEvents = eventQueue.stream()
                .map(this::serializeEvent)
                .collect(Collectors.joining("\n", "", "\n"));
        appendEventsFile(newEvents);
    }
}
