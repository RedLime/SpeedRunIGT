package com.redlimerl.speedrunigt.utils;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Language;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TranslateHelper {

    public static String[] getLangFileNames() throws IOException, URISyntaxException {
        final String path = "assets/speedrunigt/lang";
        final File jarFile = new File(TranslateHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        ArrayList<String> list = Lists.newArrayList();

        if (jarFile.isFile()) {
            // Run with JAR file
            ZipInputStream zip = new ZipInputStream(TranslateHelper.class.getProtectionDomain().getCodeSource().getLocation().openStream());

            while(true) {
                ZipEntry e = zip.getNextEntry();
                if (e == null)
                    break;
                String name = e.getName();
                if (name.startsWith(path)) {
                    String[] fileName = name.split("/");
                    String file = fileName[fileName.length-1];
                    if (!file.isEmpty() && fileName.length > 1) list.add("/" + path + "/" + file);
                }
            }
        } else {
            // Run with IDE
            final URL url = TranslateHelper.class.getResource("/" + path);
            if (url != null) {
                final File apps = new File(url.toURI());
                for (File app : Objects.requireNonNull(apps.listFiles())) {
                    list.add("/" + path + "/" + app.getName());
                }
            }
        }

        return list.toArray(new String[0]);
    }

    public static void setup(String langCode, BiConsumer<String, String> biConsumer, boolean englishOnly) {
        try {
            for (String langFileName : getLangFileNames()) {
                if (englishOnly && !langFileName.endsWith("en_us.json")) continue;

                if (!langFileName.endsWith(langCode + ".json")) continue;

                InputStream inputStream = TranslateHelper.class.getResourceAsStream(langFileName);
                if (inputStream != null) {
                    Language.load(inputStream, biConsumer);
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
