package com.redlimerl.speedrunigt.config;

import com.google.common.base.Charsets;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class Options {

    public enum TimerPosition {
        NONE, LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM
    }

    private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve(SpeedRunIGT.MOD_ID);
    public static final Options INSTANCE = new Options();

    private TimerPosition timerPos = TimerPosition.LEFT_BOTTOM;

    public TimerPosition getTimerPos() {
        return timerPos;
    }

    public void setTimerPos(TimerPosition targetPos) {
        timerPos = targetPos;
        save();
    }

    public void init() {
        try {
            Files.createDirectories(configPath);

            File optionFile = new File(configPath.toFile(), "options.txt");
            if (optionFile.exists()) {
                HashMap<String, String> optionData = new HashMap<>();

                for (String line : Files.readAllLines(optionFile.toPath(), Charsets.UTF_8)) {
                    String[] op = line.split(":", 2);
                    if (op.length == 2) {
                        optionData.putIfAbsent(op[0], op[1]);
                    }
                }

                timerPos = TimerPosition.valueOf(optionData.getOrDefault("timerPos", "NONE"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            Files.createDirectories(configPath);

            File config = new File(configPath.toFile(), "options.txt");

            if (!config.exists()) {
                config.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(config);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            DataOutputStream dos = new DataOutputStream(bos);

            dos.writeUTF("timerPos:"+timerPos.name());

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
