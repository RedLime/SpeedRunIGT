package com.redlimerl.speedrunigt.instance;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.events.EventRepository;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.GameVersion;
import net.minecraft.SharedConstants;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TimerWorld {
    private static final Gson GSON = new Gson();
    private static final String EVENT_LOG_FILE_NAME = "events.log";
    private final Path worldFolderPath;
    private final List<String> mods;
    private final GameVersion version;
    private final EventRepository eventRepository;

    TimerWorld(Path worldFolderPath, Path globalEventsPath) {
        this.worldFolderPath = worldFolderPath;
        this.mods = FabricLoader.getInstance().getAllMods().stream().map(mod -> mod.getMetadata().getId()).collect(Collectors.toList());
        this.version = SharedConstants.getGameVersion();
        this.eventRepository = new EventRepository(this, this.worldFolderPath.resolve(EVENT_LOG_FILE_NAME), globalEventsPath);
    }

    public String getWorldData() {
        return GSON.toJson(this.getWorldDataObject());
    }

    private JsonObject getWorldDataObject() {
        JsonObject object = new JsonObject();
        JsonArray modsArray = new JsonArray();
        this.mods.forEach(modsArray::add);
        object.addProperty("world_path", this.worldFolderPath.getParent().toString().replace("\\", "/"));
        object.add("mods", modsArray);
        object.addProperty("version", this.version.name());
        object.addProperty("mod_version", SpeedRunIGT.MOD_VERSION);
        object.addProperty("category", InGameTimer.getInstance().getCategory().getID());
        return object;
    }

    public EventRepository getEventRepository() {
        return this.eventRepository;
    }

    /**
     * Gets the UUIDs of all players that have previously played this world
     */
    public Set<UUID> getPreviousPlayers() {
        try {
            return Arrays.stream(Objects.requireNonNull(worldFolderPath.resolveSibling("stats").toFile().list())).map(s -> UUID.fromString(s.split("\\.")[0])).collect(Collectors.toSet());
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }
}