package com.redlimerl.speedrunigt.option;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SpeedRunOptions {

    private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve(SpeedRunIGT.MOD_ID);

    static final HashMap<Identifier, OptionArgument<?>> optionArguments = new HashMap<>();

    private static final HashMap<Identifier, String> options = new HashMap<>();

    public static final OptionArgument<TimerPosition> TIMER_POS = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "timerpos"), TimerPosition.LEFT_TOP) {
        @Override
        public TimerPosition valueFromString(String string) {
            return TimerPosition.valueOf(string);
        }

        @Override
        public String valueToString(TimerPosition value) {
            return value.name();
        }
    };

    public static final OptionArgument<Boolean> ANY_PERCENT_MODE = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "any_percent"), true) {
        @Override
        public Boolean valueFromString(String string) {
            return Boolean.parseBoolean(string);
        }

        @Override
        public String valueToString(Boolean value) {
            return Boolean.toString(value);
        }
    };

    public static <T> T getOption(OptionArgument<T> option) {
        return options.containsKey(option.getKey()) ? option.valueFromString(options.get(option.getKey())) : option.getDefaultValue();
    }

    public static <T> void setOption(OptionArgument<T> option, T value) {
        options.put(option.getKey(), option.valueToString(value));
        save();
    }

    public static void init() {
        try {
            Files.createDirectories(configPath);

            File optionFile = new File(configPath.toFile(), "options.txt");
            if (optionFile.exists()) {
                String optionData = FileUtils.readFileToString(optionFile, StandardCharsets.UTF_8);

                for (String s : optionData.split("\n")) {
                    String[] od = s.split(":", 3);
                    if (od.length == 3) {
                        options.put(new Identifier(od[0], od[1]), od[2]);
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
            options.forEach((key, value) -> stringBuilder.append(key.toString()).append(":").append(value).append("\n"));
            FileUtils.writeStringToFile(config, stringBuilder.length() == 0 ? "" : stringBuilder.substring(0, stringBuilder.length()-1), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ArrayList<Function<Screen, ClickableWidget>> buttons = new ArrayList<>();
    static HashMap<Function<Screen, ClickableWidget>, List<Text>> tooltips = new HashMap<>();
    public static void addOptionButton(Function<Screen, ClickableWidget> consumer, Text... texts) {
        buttons.add(consumer);
        tooltips.put(consumer, Arrays.stream(texts).toList());
    }
}
