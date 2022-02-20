package com.redlimerl.speedrunigt.utils;

import com.google.gson.reflect.TypeToken;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.LanguageManager;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TranslateHelper {
    private static final HashMap<String, Map<String, String>> LANGUAGE_MAPS = new HashMap<>();
    private static final ArrayList<String> LANGUAGE_KEYS = new ArrayList<>();
    private static final String DEFAULT_LANG = "en_us";

    public static void init() throws Throwable {
        LANGUAGE_MAPS.clear();
        LANGUAGE_KEYS.clear();

        Type type = new TypeToken<Map<String, String>>(){}.getType();

        for (String langFileName : getLangFileNames()) {
            if (langFileName.isEmpty()) continue;
            Map<String, String> translations = SpeedRunIGT.GSON.fromJson(getResource("/lang/"+langFileName), type);
            for (String key : translations.keySet()) {
                if (!LANGUAGE_KEYS.contains(key)) LANGUAGE_KEYS.add(key);
            }
            LANGUAGE_MAPS.put(langFileName.replace(".json", ""), translations);
        }
    }


    public static String translate(String key) {
        LanguageManager languageManager = MinecraftClient.getInstance().getLanguageManager();
        String languageCode = DEFAULT_LANG;
        if (languageManager != null) {
            languageCode = languageManager.getLanguage().getCode();
            if (!LANGUAGE_MAPS.containsKey(languageCode)
                    || !LANGUAGE_MAPS.get(languageCode).containsKey(key)) {
                languageCode = DEFAULT_LANG;
            }
        }

        return LANGUAGE_MAPS.get(languageCode).getOrDefault(key, key);
    }

    public static boolean hasTranslate(String key) {
        return LANGUAGE_KEYS.contains(key);
    }



    private static String getResource(String path) throws IOException {
        InputStream langInputStream = TranslateHelper.class.getResourceAsStream(path);
        if (langInputStream == null) throw new IOException("'" + path + "' directory is null");

        String result = IOUtils.toString(langInputStream, StandardCharsets.UTF_8);
        langInputStream.close();
        return result;
    }

    public static String[] getLangFileNames() throws IOException {
        if (FabricLoader.getInstance().isDevelopmentEnvironment())
            return getResource("/lang").split("\n");

        CodeSource src = TranslateHelper.class.getProtectionDomain().getCodeSource();
        ArrayList<String> list = new ArrayList<>();
        if (src != null) {
            URL jar = src.getLocation();
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            while(true) {
                ZipEntry e = zip.getNextEntry();
                if (e == null)
                    break;
                String name = e.getName();
                if (name.startsWith("lang")) {
                    String[] fileName = name.split("/");
                    String file = fileName[fileName.length-1];
                    if (!file.isEmpty() && fileName.length > 1) list.add(fileName[fileName.length-1]);
                }
            }
        }

        return list.toArray(new String[0]);
    }
}
