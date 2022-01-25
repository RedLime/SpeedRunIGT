package com.redlimerl.speedrunigt.option;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.RunCategory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public class SpeedRunOptions {

    private static boolean isInit = false;

    private static final Path oldConfigPath = FabricLoader.getInstance().getConfigDir().resolve(SpeedRunIGT.MOD_ID);
    private static final Path configPath = SpeedRunIGT.getMainPath().resolve("options.txt");

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
            if (new File(oldConfigPath.toFile(), "options.txt").exists() && !configPath.toFile().exists()) {
                File optionFile = new File(oldConfigPath.toFile(), "options.txt");
                FileUtils.copyFile(optionFile, configPath.toFile());
                FileUtils.deleteQuietly(optionFile);
            }

            File optionFile = configPath.toFile();
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
            File config = configPath.toFile();
            StringBuilder stringBuilder = new StringBuilder();
            options.forEach((key, value) -> stringBuilder.append(key.toString()).append(":").append(value).append("\n"));
            FileUtils.writeStringToFile(config, stringBuilder.length() == 0 ? "" : stringBuilder.substring(0, stringBuilder.length()-1), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Function<Screen, AbstractButtonWidget>> buttons = new ArrayList<>();
    public static HashMap<Function<Screen, AbstractButtonWidget>, List<Text>> tooltips = new HashMap<>();
    public static void addOptionButton(Function<Screen, AbstractButtonWidget> consumer, Text... texts) {
        buttons.add(consumer);
        tooltips.put(consumer, Arrays.asList(texts));
    }




    public static final OptionArgument<RunCategory> TIMER_CATEGORY = new OptionArgument<RunCategory>(new Identifier(SpeedRunIGT.MOD_ID, "timer_category"), RunCategory.ANY) {
        @Override
        public RunCategory valueFromString(String string) {
            try {
                return RunCategory.valueOf(string);
            } catch (IllegalArgumentException exception) {
                return RunCategory.ANY;
            }
        }

        @Override
        public String valueToString(RunCategory value) {
            return value.name();
        }
    };

    public static final OptionArgument<Boolean> DISPLAY_TIME_ONLY = new OptionArgument<Boolean>(new Identifier(SpeedRunIGT.MOD_ID, "display_time_only"), false) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public enum TimerDecimals { NONE(0), ONE(1), TWO(2), THREE(3);
        private final int number;
        TimerDecimals(int number) {
            this.number = number;
        }
        public int getNumber() {
            return number;
        }
    }
    public static final OptionArgument<TimerDecimals> DISPLAY_DECIMALS = new OptionArgument<TimerDecimals>(new Identifier(SpeedRunIGT.MOD_ID, "timer_display_decimals"), TimerDecimals.THREE) {
        @Override
        public TimerDecimals valueFromString(String string) {
            return TimerDecimals.valueOf(string);
        }

        @Override
        public String valueToString(TimerDecimals value) {
            return value.name();
        }
    };

    public static final OptionArgument<Boolean> LOCK_TIMER_POSITION = new OptionArgument<Boolean>(new Identifier(SpeedRunIGT.MOD_ID, "lock_timer_position"), false) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Identifier> TIMER_TEXT_FONT = new OptionArgument<Identifier>(new Identifier(SpeedRunIGT.MOD_ID, "timer_text_font"), MinecraftClient.DEFAULT_TEXT_RENDERER_ID) {
        @Override
        public Identifier valueFromString(String string) {
            return Identifier.tryParse(string);
        }

        @Override
        public String valueToString(Identifier value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Boolean> TOGGLE_TIMER = new OptionArgument<Boolean>(new Identifier(SpeedRunIGT.MOD_ID, "toggle_timer"), true) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Boolean> WAITING_FIRST_INPUT = new OptionArgument<Boolean>(new Identifier(SpeedRunIGT.MOD_ID, "waiting_first_input"), false) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Boolean> HIDE_TIMER_IN_OPTIONS = new OptionArgument<Boolean>(new Identifier(SpeedRunIGT.MOD_ID, "hide_timer_in_options"), true) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Boolean> HIDE_TIMER_IN_DEBUGS = new OptionArgument<Boolean>(new Identifier(SpeedRunIGT.MOD_ID, "hide_timer_in_debugs"), false) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Float> TIMER_RTA_POSITION_X = new OptionArgument<Float>(new Identifier(SpeedRunIGT.MOD_ID, "timer_rta_pos_x"), 0.017f) {
        @Override
        public Float valueFromString(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public String valueToString(Float value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Float> TIMER_RTA_POSITION_Y = new OptionArgument<Float>(new Identifier(SpeedRunIGT.MOD_ID, "timer_rta_pos_y"), 0.035f) {
        @Override
        public Float valueFromString(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public String valueToString(Float value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Float> TIMER_IGT_POSITION_X = new OptionArgument<Float>(new Identifier(SpeedRunIGT.MOD_ID, "timer_igt_pos_x"), 0.017f) {
        @Override
        public Float valueFromString(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public String valueToString(Float value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Float> TIMER_IGT_POSITION_Y = new OptionArgument<Float>(new Identifier(SpeedRunIGT.MOD_ID, "timer_igt_pos_y"), 0.08f) {
        @Override
        public Float valueFromString(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public String valueToString(Float value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Float> TIMER_IGT_SCALE = new OptionArgument<Float>(new Identifier(SpeedRunIGT.MOD_ID, "timer_scale_igt"), 1.0f) {
        @Override
        public Float valueFromString(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public String valueToString(Float value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Float> TIMER_RTA_SCALE = new OptionArgument<Float>(new Identifier(SpeedRunIGT.MOD_ID, "timer_scale_rta"), 1.0f) {
        @Override
        public Float valueFromString(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public String valueToString(Float value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Integer> TIMER_IGT_COLOR = new OptionArgument<Integer>(new Identifier(SpeedRunIGT.MOD_ID, "timer_igt_color"), Formatting.YELLOW.getColorValue()) {
        @Override
        public Integer valueFromString(String string) {
            return Integer.parseInt(string);
        }

        @Override
        public String valueToString(Integer value) {
            return String.valueOf(value);
        }
    };

    public static final OptionArgument<Integer> TIMER_RTA_COLOR = new OptionArgument<Integer>(new Identifier(SpeedRunIGT.MOD_ID, "timer_rta_color"), Formatting.AQUA.getColorValue()) {
        @Override
        public Integer valueFromString(String string) {
            return Integer.parseInt(string);
        }

        @Override
        public String valueToString(Integer value) {
            return String.valueOf(value);
        }
    };

    public enum TimerDecoration { NONE, OUTLINE, SHADOW }
    public static final OptionArgument<TimerDecoration> TIMER_RTA_DECO = new OptionArgument<TimerDecoration>(new Identifier(SpeedRunIGT.MOD_ID, "timer_rta_decoration"), TimerDecoration.OUTLINE) {
        @Override
        public TimerDecoration valueFromString(String string) {
            return TimerDecoration.valueOf(string);
        }

        @Override
        public String valueToString(TimerDecoration value) {
            return value.name();
        }
    };

    public static final OptionArgument<TimerDecoration> TIMER_IGT_DECO = new OptionArgument<TimerDecoration>(new Identifier(SpeedRunIGT.MOD_ID, "timer_igt_decoration"), TimerDecoration.OUTLINE) {
        @Override
        public TimerDecoration valueFromString(String string) {
            return TimerDecoration.valueOf(string);
        }

        @Override
        public String valueToString(TimerDecoration value) {
            return value.name();
        }
    };

    public static final OptionArgument<Integer> RTA_BACKGROUND_PADDING = new OptionArgument<Integer>(new Identifier(SpeedRunIGT.MOD_ID, "rta_bg_padding"), 3) {
        @Override
        public Integer valueFromString(String string) {
            return Integer.parseInt(string);
        }

        @Override
        public String valueToString(Integer value) {
            return String.valueOf(value);
        }
    };

    public static final OptionArgument<Integer> IGT_BACKGROUND_PADDING = new OptionArgument<Integer>(new Identifier(SpeedRunIGT.MOD_ID, "igt_bg_padding"), 3) {
        @Override
        public Integer valueFromString(String string) {
            return Integer.parseInt(string);
        }

        @Override
        public String valueToString(Integer value) {
            return String.valueOf(value);
        }
    };

    public static final OptionArgument<Float> BACKGROUND_OPACITY = new OptionArgument<Float>(new Identifier(SpeedRunIGT.MOD_ID, "bgf_opacity"), 0f) {
        @Override
        public Float valueFromString(String string) {
            return Float.parseFloat(string);
        }

        @Override
        public String valueToString(Float value) {
            return String.valueOf(value);
        }
    };
}
