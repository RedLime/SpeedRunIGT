package com.redlimerl.speedrunigt.instance;


import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.events.MemoryBufferedEventRepository;
import com.redlimerl.speedrunigt.events.EventRepository;
import com.redlimerl.speedrunigt.events.FileEventRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class World {
    private static final String EVENT_LOG_FILE_NAME = "events.log";

    public final MemoryBufferedEventRepository eventRepository;

    World(Path worldFolderPath) {
        Path worldTimerFolderPath = worldFolderPath.resolve(SpeedRunIGT.MOD_ID);

        if (Files.notExists(worldFolderPath) || !Files.isDirectory(worldFolderPath)) {
            SpeedRunIGT.error("World directory doesn't exist, couldn't make timer dirs");
        }

        if (Files.notExists(worldTimerFolderPath)) {
            try {
                Files.createDirectory(worldTimerFolderPath);
                SpeedRunIGT.debug("world's timer directory doesn't exist, made timer dirs");
            } catch (IOException e) {
                SpeedRunIGT.debug("couldn't make world's timer directory");
            }
        }
        else if (!Files.isDirectory(worldTimerFolderPath)) {
            this.eventRepository = null;
            SpeedRunIGT.error("Invalid world's timer directory, try remove this file: " + worldTimerFolderPath);
            return;
        }

        Path eventLogPath = worldTimerFolderPath.resolve(EVENT_LOG_FILE_NAME);
        EventRepository fileEventRepository = new FileEventRepository(eventLogPath);
        this.eventRepository = new MemoryBufferedEventRepository(fileEventRepository);
        // TODO: create world state repository here
    }

    static class WorldStateRepository {
        // TODO: create api to store meta information about run that is not safe to be publicly accessible here
    }
}