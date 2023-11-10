package com.redlimerl.speedrunigt.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Language;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.BiConsumer;

public class TranslateHelper {
    public static String[] getLangFileNames() throws IOException, URISyntaxException {
        return ResourcesHelper.getResourceChildren("lang");
    }

    public static void setup(List<Resource> resources, BiConsumer<String, String> biConsumer, boolean englishOnly) {
        try {
            for (String langFileName : getLangFileNames()) {
                if (englishOnly && !langFileName.endsWith("en_us.json")) continue;

                for (Resource resource : resources) {
                    if (!langFileName.endsWith(resource.getId().getPath())) continue;

                    InputStream inputStream = TranslateHelper.class.getResourceAsStream(langFileName);
                    if (inputStream != null) {
                        Language.load(inputStream, biConsumer);
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        MinecraftClient.getInstance().reloadResources();
    }
}
