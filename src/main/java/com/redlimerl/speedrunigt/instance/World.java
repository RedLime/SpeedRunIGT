package com.redlimerl.speedrunigt.instance;


import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.events.MemoryBufferedEventRepository;
import com.redlimerl.speedrunigt.events.EventRepository;
import com.redlimerl.speedrunigt.events.FileEventRepository;

import java.nio.file.Files;
import java.nio.file.Path;

public class World {
    private static final String EVENT_LOG_FILE_NAME = "events.log";

    public final MemoryBufferedEventRepository eventRepository;

    World(Path worldFolderPath) {
        Path worldTimerFolderPath = worldFolderPath.resolve(SpeedRunIGT.MOD_ID);

        if (Files.notExists(worldFolderPath) || !Files.isDirectory(worldFolderPath)) {
            SpeedRunIGT.error("World directory doesn't exist, couldn't make timer dirs");
        }

        if (Files.notExists(worldFolderPath.resolve(SpeedRunIGT.MOD_ID))) {
            // TODO: create timer folder and log
        }
        else if (!Files.isDirectory(worldTimerFolderPath)) {
            // TODO: throw error
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