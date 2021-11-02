package com.redlimerl.speedrunigt.config;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class SpeedRunOptions {

    public static abstract class OptionArgument<T> {
        private final Identifier key;
        private final T defaultValue;

        public OptionArgument(Identifier key, T defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public Identifier getKey() {
            return key;
        }

        public OptionArgument<T> register() {
            optionArguments.put(this.getKey(), this);
            return this;
        }

        public abstract T valueFromString(String string);

        public abstract String valueToString(T value);
    }

    public enum TimerPosition {
        NONE, LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM
    }

    private static final HashMap<Identifier, OptionArgument<?>> optionArguments = new HashMap<>();

    private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve(SpeedRunIGT.MOD_ID);
    public static final OptionArgument<TimerPosition> TIMER_POS = new OptionArgument<TimerPosition>(new Identifier(SpeedRunIGT.MOD_ID, "timerpos"), TimerPosition.LEFT_TOP) {
        @Override
        public TimerPosition valueFromString(String string) {
            return TimerPosition.valueOf(string);
        }

        @Override
        public String valueToString(TimerPosition value) {
            return value.name();
        }
    }.register();

    public static HashMap<OptionArgument<?>, String> options = new HashMap<>();

    public static <T> T getOption(OptionArgument<T> option) {
        return options.containsKey(option) ? option.valueFromString(options.get(option)) : option.getDefaultValue();
    }

    public static <T> void setOption(OptionArgument<T> option, T value) {
        options.put(option, option.valueToString(value));
        save();
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    public static void init() {
        try {
            Files.createDirectories(configPath);

            File optionFile = new File(configPath.toFile(), "options.txt");
            if (optionFile.exists()) {
                String optionData = FileUtils.readFileToString(optionFile, StandardCharsets.UTF_8);

                for (String s : optionData.split("\n")) {
                    String[] od = s.split(":", 3);
                    if (od.length == 3) {
                        OptionArgument<?> id = optionArguments.get(new Identifier(od[0], od[1]));
                        options.put(id, od[2]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(configPath);

            File config = new File(configPath.toFile(), "options.txt");
            StringBuilder stringBuilder = new StringBuilder();
            options.forEach((key, value) -> stringBuilder.append(key.getKey().toString()).append(":").append(value).append("\n"));
            FileUtils.writeStringToFile(config, stringBuilder.length() == 0 ? "" : stringBuilder.substring(0, stringBuilder.length()-1), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
