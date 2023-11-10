package com.redlimerl.speedrunigt.events;

import com.redlimerl.speedrunigt.utils.MonadicStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Event {
    @NotNull public final String id;
    @NotNull public final Integer version;
    @NotNull public final String type;
    @NotNull public final Long gameTime;
    @NotNull public final Long realTime;

    public Event(
            @NotNull Integer eventVersion,
            @NotNull String eventId,
            @NotNull String type,
            @NotNull Long gameTime,
            @NotNull Long realTime
    ) {
        this.version = eventVersion;
        this.id = eventId;
        this.type = type;
        this.gameTime = gameTime;
        this.realTime = realTime;
    }

    public static @Nullable Event parse(String eventString, Map<String, Integer> runningVersions) {
        try {
            String[] parts = eventString.trim().split(" ");

            String eventId;
            Integer version = 0;
            String type;
            int offset;
            try {
                eventId = parts[0];
                type = parts[1];
                offset = 2;
            } catch (Exception ignored) {
                eventId = parts[0];
                version = runningVersions.getOrDefault(eventId, 0);
                type = "common";
                offset = 1;
            }

            Long gameTime = Long.parseLong(parts[offset++]);
            Long realTime = Long.parseLong(parts[offset++]);
            if (parts.length >= offset + 1) {
                version = Integer.parseInt(parts[offset + 1]);
            }

            return new Event(version, eventId, type, gameTime, realTime);
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, String> decodeDataString(@Nullable String data) {
        Map<String, String> dataMap = new HashMap<>();
        if (data == null) { return dataMap; }
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
