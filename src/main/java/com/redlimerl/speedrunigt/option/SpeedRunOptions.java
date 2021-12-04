package com.redlimerl.speedrunigt.option;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.RunCategory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class SpeedRunOptions {

    private static boolean isInit = false;

    private static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve(SpeedRunIGT.MOD_ID);

    private static final HashMap<Identifier, String> options = new HashMap<>();

    public static <T> T getOption(OptionArgument<T> option) {
        if (!isInit) init();
        return options.containsKey(option.getKey()) ? option.valueFromString(options.get(option.getKey())) : option.getDefaultValue();
    }

    public static <T> void setOption(OptionArgument<T> option, T value) {
        options.put(option.getKey(), option.valueToString(value));
        save();
    }

    public static void init() {
        if (isInit) return;

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
            isInit = true;
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



    public static final OptionArgument<RunCategory> TIMER_CATEGORY = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "timer_category"), RunCategory.ANY) {
        @Override
        public RunCategory valueFromString(String string) {
            return RunCategory.valueOf(string);
        }

        @Override
        public String valueToString(RunCategory value) {
            return value.name();
        }
    };

    public static final OptionArgument<Float> TIMER_POSITION_X = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "timer_pos_x"), 0.035f) {
        @Override
        public Float valueFromString(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public String valueToString(Float value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Float> TIMER_POSITION_Y = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "timer_pos_y"), 0.035f) {
        @Override
        public Float valueFromString(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public String valueToString(Float value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Float> TIMER_IGT_SCALE = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "timer_scale_igt"), 1.0f) {
        @Override
        public Float valueFromString(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public String valueToString(Float value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Float> TIMER_RTA_SCALE = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "timer_scale_rta"), 1.0f) {
        @Override
        public Float valueFromString(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public String valueToString(Float value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Float> TIMER_BG_OPACITY = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "timer_bg_opacity"), 0.4f) {
        @Override
        public Float valueFromString(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public String valueToString(Float value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Boolean> REVERSED_IGT_RTA = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "revered_timer"), false) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Boolean> DISPLAY_TIME_ONLY = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "display_time_only"), false) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Boolean> TOGGLE_TIMER = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "toggle_timer"), true) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Boolean> WAITING_FIRST_INPUT = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "waiting_first_input"), false) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Integer> TIMER_IGT_COLOR = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "timer_igt_color"), Formatting.YELLOW.getColorValue()) {
        @Override
        public Integer valueFromString(String string) {
            return Integer.parseInt(string);
        }

        @Override
        public String valueToString(Integer value) {
            return String.valueOf(value);
        }
    };

    public static final OptionArgument<Boolean> TIMER_IGT_OUTLINE = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "timer_igt_outline"), true) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Integer> TIMER_RTA_COLOR = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "timer_rta_color"), Formatting.AQUA.getColorValue()) {
        @Override
        public Integer valueFromString(String string) {
            return Integer.parseInt(string);
        }

        @Override
        public String valueToString(Integer value) {
            return String.valueOf(value);
        }
    };

    public static final OptionArgument<Boolean> TIMER_RTA_OUTLINE = new OptionArgument<>(new Identifier(SpeedRunIGT.MOD_ID, "timer_rta_outline"), true) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };
}
