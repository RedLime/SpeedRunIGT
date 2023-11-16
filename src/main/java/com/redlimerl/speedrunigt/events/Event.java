package com.redlimerl.speedrunigt.events;

import com.redlimerl.speedrunigt.utils.MonadicStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Event {
    private static final Logger LOGGER = LogManager.getLogger("Event");
    @NotNull public final String id;
    @NotNull public final Integer version;
    @NotNull public final String type;
    @NotNull public final Boolean repeatable;
    @NotNull public final Long realTime;
    @NotNull public final Long gameTime;

    public Event(
            @NotNull Integer eventVersion,
            @NotNull String eventId,
            @NotNull String type,
            @NotNull Boolean repeatable,
            @NotNull Long realTime,
            @NotNull Long gameTime
    ) {
        this.version = eventVersion;
        this.id = eventId;
        this.type = type;
        this.repeatable = repeatable;
        this.realTime = realTime;
        this.gameTime = gameTime;
    }

    public static @Nullable Event parse(String eventString, Map<String, Integer> runningVersions) {
        try {
            String[] parts = eventString.trim().split(" ");

            int wordPointer = 0;
            String eventId = parts[wordPointer++];
            String type = eventId.split("\\.")[1];
            long realTime = Long.parseLong(parts[wordPointer++]);
            long gameTime = Long.parseLong(parts[wordPointer++]);
            int version = runningVersions.getOrDefault(eventId, 0);
            if (wordPointer < parts.length) {
                version = Integer.parseInt(parts[wordPointer]);
            }

            return new Event(version, eventId, type, EventFactoryLoader.isEventRepeatable(eventId, type), realTime, gameTime);
        } catch (Exception e) {
            LOGGER.error("Error while parsing event", e);
            return null;
        }
    }

    public static Map<String, String> decodeDataString(@Nullable String data) {
        Map<String, String> dataMap = new HashMap<>();
        if (data == null) {
            return dataMap;
        }
        String[] parts = (data.endsWith(";") ? data.substring(0, data.length() - 1) : data).split(";");
        for (String part : parts) {
            String[] kv = part.split(":");
            dataMap.put(kv[0], kv[1]);
        }
        return dataMap;
    }

    public String serialize(boolean writeVersion) {
        MonadicStringBuilder stringBuilder = new MonadicStringBuilder();

        return stringBuilder
                .append(this.id)
                .append(" " + this.realTime)
                .append(" " + this.gameTime)
                .appendIf(() -> writeVersion, " " + this.version)
                .toString();
    }

    public @NotNull Long getRealTime() {
        return this.realTime;
    }
}
