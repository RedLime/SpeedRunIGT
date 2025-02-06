package com.redlimerl.speedrunigt.events;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redlimerl.speedrunigt.utils.ResourcesHelper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventFactoryLoader {
    private static final Logger LOGGER = LogManager.getLogger("Event Factory Loader");
    private static final JsonParser PARSER = new JsonParser();
    private static final Map<String, List<EventFactory>> TYPE_TO_FACTORIES = new HashMap<>();

    public static List<EventFactory> getEventFactories(String type) {
        List<EventFactory> factories = null;
        if (!TYPE_TO_FACTORIES.containsKey(type) || (factories = TYPE_TO_FACTORIES.get(type)).isEmpty()) {
            loadEventFactories();
        }
        return factories == null ? TYPE_TO_FACTORIES.get(type) : factories;
    }

    public static boolean isEventRepeatable(String eventId, String type) {
        return getEventFactories(type).stream().filter(ef -> ef.eventId.equals(eventId)).findAny().map(ef -> ef.repeatable).orElse(false);
    }

    public static boolean isEventRepeatable(Event event) {
        return isEventRepeatable(event.id, event.type);
    }

    private static void loadEventFactories() {
        try {
            String[] resourceNames = ResourcesHelper.getResourceChildren("events");
            for (String resource : resourceNames) {
                String[] parts = resource.split("/");
                String source = parts[parts.length - 1].replace(".json", "");

                InputStream stream = ResourcesHelper.toStream("/" + resource);
                String content = IOUtils.toString(stream, Charset.defaultCharset());
                JsonArray eventsArray = PARSER.parse(content).getAsJsonArray();
                for (JsonElement element : eventsArray) {
                    JsonObject eventObject = element.getAsJsonObject();
                    StringBuilder data = new StringBuilder();
                    if (eventObject.has("data")) {
                        JsonObject dataObject = eventObject.get("data").getAsJsonObject();
                        for (Map.Entry<String, JsonElement> dataEntry : dataObject.entrySet()) {
                            String key = dataEntry.getKey();
                            String value = dataEntry.getValue().getAsString();
                            data.append(key).append(":").append(value).append(";");
                        }
                    }
                    String type = eventObject.get("type").getAsString();
                    JsonElement repeatableEl = eventObject.get("repeatable");
                    boolean repeatable = repeatableEl != null && repeatableEl.getAsBoolean();
                    EventFactory factory = new EventFactory(
                            source,
                            eventObject.get("name").getAsString(),
                            type,
                            repeatable,
                            (data.length() == 0) ? null : data.toString()
                    );
                    if (!TYPE_TO_FACTORIES.containsKey(type)) {
                        TYPE_TO_FACTORIES.put(type, Lists.newArrayList(factory));
                    } else {
                        List<EventFactory> list = TYPE_TO_FACTORIES.get(type);
                        list.add(factory);
                        TYPE_TO_FACTORIES.put(type, list);
                    }
                }
                stream.close();
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Error while loading events", e);
        }
    }
}
