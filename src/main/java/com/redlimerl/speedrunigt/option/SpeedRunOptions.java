package com.redlimerl.speedrunigt.option;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class SpeedRunOptions {
    public static final OptionArgument<RunCategory> TIMER_CATEGORY = new OptionArgument<RunCategory>(new Identifier(SpeedRunIGT.MOD_ID, "timer_category_v7"), RunCategories.ANY) {
        @Override
        public RunCategory valueFromString(String string) {
            RunCategory category = RunCategory.getCategory(string);
            return category == RunCategories.ERROR_CATEGORY ? RunCategories.ANY : category;
        }

        @Override
        public String valueToString(RunCategory value) {
            return value.getID();
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

    public static final OptionArgument<Boolean> AUTOMATIC_COOP_MODE = new OptionArgument<Boolean>(new Identifier(SpeedRunIGT.MOD_ID, "auto_coop_toggle"), true) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Boolean> TIMER_START_GENERATED_WORLD = new OptionArgument<Boolean>(new Identifier(SpeedRunIGT.MOD_ID, "start_generated_world"), false) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Boolean> TIMER_LIMITLESS_RESET = new OptionArgument<Boolean>(new Identifier(SpeedRunIGT.MOD_ID, "limitless_reset"), false) {
        @Override
        public Boolean valueFromString(String string) {
            return Objects.equals(string, "true");
        }

        @Override
        public String valueToString(Boolean value) {
            return value.toString();
        }
    };

    public static final OptionArgument<Float> TIMER_RTA_POSITION_X = new OptionArgument<Float>(new Identifier(SpeedRunIGT.MOD_ID, "timer_rta_pos_x"), 0.983f) {
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

    public static final OptionArgument<Float> TIMER_IGT_POSITION_X = new OptionArgument<Float>(new Identifier(SpeedRunIGT.MOD_ID, "timer_igt_pos_x"), 0.983f) {
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

    public enum SplitDisplayType { NONE, MESSAGE, TOAST }
    public static final OptionArgument<SplitDisplayType> SPLIT_DISPLAY_TYPE = new OptionArgument<SplitDisplayType>(new Identifier(SpeedRunIGT.MOD_ID, "split_display_type"), SplitDisplayType.NONE) {
        @Override
        public SplitDisplayType valueFromString(String string) {
            return SplitDisplayType.valueOf(string);
        }

        @Override
        public String valueToString(SplitDisplayType value) {
            return value.name();
        }
    };
}
