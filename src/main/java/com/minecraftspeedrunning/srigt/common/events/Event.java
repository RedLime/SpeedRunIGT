package com.minecraftspeedrunning.srigt.common.events;

import com.redlimerl.speedrunigt.utils.MonadicStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Event {
    public static @Nullable Event parse(String eventString, Map<String, Integer> runningVersions) {
        try {
            // TODO: deal with escaped spaces
            String[] parts = eventString.split(" ");

            String eventId;
            Integer version;
            int offset;
            try {
                version = Integer.parseInt(parts[0]);
                eventId = parts[1];
                offset = 2;
            }
            catch (Exception ignored) {
                eventId = parts[0];
                version = runningVersions.getOrDefault(eventId, 0);
                offset = 1;
            }

            Long realTime = Long.parseLong(parts[offset]);
            Long gameTime = Long.parseLong(parts[offset + 1]);
            String data = parts[offset + 2];

            return new Event(version, eventId, realTime, gameTime, data);
        }
        catch (Exception e) {
            return null;
        }
    }

    @NotNull
    public final String id;
    @NotNull
    public final Integer version;
    @NotNull
    public final Long gameTime;
    @NotNull
    public final Long realTime;
    @Nullable
    public final String data;

    Event(
        @NotNull Integer eventVersion,
        @NotNull String eventId,
        @NotNull Long gameTime,
        @NotNull Long realTime
    ) {
        this.id = eventId;
        this.version = eventVersion;
        this.realTime = realTime;
        this.gameTime = gameTime;
        this.data = null;
    }

    Event(
        @NotNull Integer eventVersion,
        @NotNull String eventId,
        @NotNull Long gameTime,
        @NotNull Long realTime,
        String data
    ) {
        this.id = eventId;
        this.version = eventVersion;
        this.realTime = realTime;
        this.gameTime = gameTime;
        this.data = data;
    }

    public String serialize(Boolean writeVersion) {
        MonadicStringBuilder stringBuilder = new MonadicStringBuilder();

        return stringBuilder
            .appendIf(() -> writeVersion, " " + version)
            .append(id)
            .append(" " + realTime)
            .append(" " + gameTime)
            .appendIf(() -> data != null, " " + data)
            .toString();
    }

    public @NotNull Long getRealTime() {
        return realTime;
    }
}
