package com.redlimerl.speedrunigt.option;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SpeedRunOption {
    private static boolean isInit = false;

    private static final Path oldConfigPath = FabricLoader.getInstance().getConfigDir().resolve(SpeedRunIGT.MOD_ID);
    public static final Path configPath = SpeedRunIGT.getMainPath().resolve("options.txt");
    public static final Path globalConfigPath;
    private static final Path useGlobalPath = SpeedRunIGT.getMainPath().resolve(".useglobaloption");
    static {
        File globalDir = new File(System.getProperty("user.home").replace("\\", "/"), SpeedRunIGT.MOD_ID);
        if (!globalDir.exists() && !globalDir.mkdirs()) {
            SpeedRunIGT.error("Failed make global config path");
        }
        globalConfigPath = globalDir.toPath().resolve("options.txt");
    }
    public static Path getConfigPath() {
        return isUsingGlobalConfig() ? globalConfigPath : configPath;
    }
    public static boolean isUsingGlobalConfig() {
        return useGlobalPath.toFile().exists();
    }
    public static void setUseGlobalConfig(boolean b) {
        try {
            if (b) FileUtils.writeStringToFile(useGlobalPath.toFile(), "", StandardCharsets.UTF_8);
            else FileUtils.deleteQuietly(useGlobalPath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final HashMap<Identifier, String> options = new HashMap<>();

    public static <T> T getOption(OptionArgument<T> option) {
        if (!isInit) init();
        return options.containsKey(option.getKey()) ? option.valueFromString(options.get(option.getKey())) : option.getDefaultValue();
    }

    public static <T> void setOption(OptionArgument<T> option, T value) {
        InGameTimerUtils.CHANGED_OPTIONS.add(option);
        options.put(option.getKey(), option.valueToString(value));
        needSave = true;
    }

    private static boolean needSave = false;
    public static void checkSave() {
        if (needSave) {
            save();
            needSave = false;
        }
    }

    public static void init() {
        if (isInit) return;

        try {
            options.clear();

            File optionFile = getConfigPath().toFile();

            if (new File(oldConfigPath.toFile(), "options.txt").exists() && !optionFile.exists()) {
                File oldOptionFile = new File(oldConfigPath.toFile(), "options.txt");
                FileUtils.copyFile(oldOptionFile, optionFile);
                FileUtils.deleteQuietly(oldOptionFile);
            }

            if (optionFile.exists()) {
                String optionData = FileUtils.readFileToString(optionFile, StandardCharsets.UTF_8);

                for (String s : optionData.split("\n")) {
                    String[] od = s.split(":", 3);
                    if (od.length == 3) {
                        options.put(new Identifier(od[0], od[1]), od[2]);
                    }
                }
            }
            isInit = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        try {
            File config = getConfigPath().toFile();
            StringBuilder stringBuilder = new StringBuilder();
            options.forEach((key, value) -> stringBuilder.append(key.toString()).append(":").append(value).append("\n"));
            FileUtils.writeStringToFile(config, stringBuilder.length() == 0 ? "" : stringBuilder.substring(0, stringBuilder.length()-1), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        isInit = false;
        init();
        SpeedRunIGTClient.TIMER_DRAWER = new TimerDrawer(false);
    }

    private static final ArrayList<OptionButtonFactory> optionButtonFactories = new ArrayList<>();
    public static List<OptionButtonFactory> getOptionButtonFactories() {
        return optionButtonFactories;
    }

    public static void addOptionButtonFactories(OptionButtonFactory... factories) {
        if (SpeedRunIGTClient.isInitialized) return;
        optionButtonFactories.addAll(Arrays.asList(factories));
    }
}
