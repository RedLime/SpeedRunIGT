package com.redlimerl.speedrunigt.world;

import com.redlimerl.speedrunigt.events.BufferedEventRepository;
import com.redlimerl.speedrunigt.events.EventRepository;

import java.nio.file.Path;

public class World {
    private final WorldFolder worldFolder;
    private final EventRepository eventRepository;

    public World(Path worldFolderPath) {
        worldFolder = new WorldFolder(worldFolderPath);
        eventRepository = new BufferedEventRepository(worldFolder);
    }


}
