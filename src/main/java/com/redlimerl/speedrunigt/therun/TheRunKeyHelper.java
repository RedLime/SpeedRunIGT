package com.redlimerl.speedrunigt.therun;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TheRunKeyHelper {

    public static final File KEY_FILE = SpeedRunIGT.getGlobalPath().resolve("therun.gg.txt").toFile();
    public static String UPLOAD_KEY = "";

    public static void load() {
        if (KEY_FILE.exists()) {
            try {
                UPLOAD_KEY = FileUtils.readFileToString(KEY_FILE, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                SpeedRunIGT.error("Failed to read file for therun.gg.key");
            }
        } else {
            try {
                FileUtils.writeStringToFile(KEY_FILE, "", StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                SpeedRunIGT.error("Failed to write file for therun.gg.key");
            }
        }
    }

    public static void save() {
        try {
            FileUtils.writeStringToFile(KEY_FILE, UPLOAD_KEY, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            SpeedRunIGT.error("Failed to save file for therun.gg.key");
        }
    }

}
