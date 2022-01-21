package com.redlimerl.speedrunigt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunCategoryScreen;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunIGTInfoScreen;
import com.redlimerl.speedrunigt.gui.screen.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.utils.FontIdentifier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.HashMap;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class SpeedRunIGT implements ClientModInitializer {

    public static final String MOD_ID = "speedrunigt";
    public static final Identifier BUTTON_ICON_TEXTURE = new Identifier(MOD_ID, "textures/gui/buttons.png");
    public static final TimerDrawer TIMER_DRAWER = new TimerDrawer(true);

    public static String DEBUG_DATA = "";
    public static String MOD_VERSION;
    public static HashMap<Identifier, FontIdentifier> FONT_MAPS = new HashMap<>();

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path WORLDS_PATH = FabricLoader.getInstance().getGameDir().resolve("saves");
    public static final Path TIMER_PATH = getMainPath().resolve("worlds");
    public static final Path FONT_PATH = getMainPath().resolve("fonts");

    public static Path getMainPath() {
        return FabricLoader.getInstance().getGameDir().resolve(MOD_ID);
    }


    static {
        getMainPath().toFile().mkdirs();
        TIMER_PATH.toFile().mkdirs();
        FONT_PATH.toFile().mkdirs();
    }

    public static KeyBinding timerResetKeyBinding;
    public static KeyBinding timerStopKeyBinding;

    @Override
    public void onInitializeClient() {
        MOD_VERSION = (FabricLoader.getInstance().getModContainer(SpeedRunIGT.MOD_ID).isPresent()
                        ? FabricLoader.getInstance().getModContainer(SpeedRunIGT.MOD_ID).get().getMetadata().getVersion().getFriendlyString() : "Unknown+Unknown");
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.timer_position"), (ButtonWidget button) -> MinecraftClient.getInstance().setScreen(new TimerCustomizeScreen(screen)))
        );
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.timer_category"), (ButtonWidget button) -> MinecraftClient.getInstance().setScreen(new SpeedRunCategoryScreen(screen)))
        );
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.check_info"), (ButtonWidget button) -> MinecraftClient.getInstance().setScreen(new SpeedRunIGTInfoScreen(screen)))
        );
        SpeedRunOptions.addOptionButton(screen -> new ButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.timer_position.toggle_timer").append(" : ").append(TIMER_DRAWER.isToggle() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
            TIMER_DRAWER.setToggle(!TIMER_DRAWER.isToggle());
            SpeedRunOptions.setOption(SpeedRunOptions.TOGGLE_TIMER, TIMER_DRAWER.isToggle());
            button.setMessage(new TranslatableText("speedrunigt.option.timer_position.toggle_timer").append(" : ").append(TIMER_DRAWER.isToggle() ? ScreenTexts.ON : ScreenTexts.OFF));
        }));
        SpeedRunOptions.addOptionButton(screen -> new ButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.hide_timer_in_options").append(" : ").append(SpeedRunOptions.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS) ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
            SpeedRunOptions.setOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS, !SpeedRunOptions.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS));
            button.setMessage(new TranslatableText("speedrunigt.option.hide_timer_in_options").append(" : ").append(SpeedRunOptions.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS) ? ScreenTexts.ON : ScreenTexts.OFF));
        }));
        SpeedRunOptions.addOptionButton(screen -> new ButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.hide_timer_in_debugs").append(" : ").append(SpeedRunOptions.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
            SpeedRunOptions.setOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS, !SpeedRunOptions.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS));
            button.setMessage(new TranslatableText("speedrunigt.option.hide_timer_in_debugs").append(" : ").append(SpeedRunOptions.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) ? ScreenTexts.ON : ScreenTexts.OFF));
        }));
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.waiting_first_input").append(" : ").append(SpeedRunOptions.getOption(SpeedRunOptions.WAITING_FIRST_INPUT) ? ScreenTexts.ON : ScreenTexts.OFF),
                (ButtonWidget button) -> {
                    SpeedRunOptions.setOption(SpeedRunOptions.WAITING_FIRST_INPUT, !SpeedRunOptions.getOption(SpeedRunOptions.WAITING_FIRST_INPUT));
                    button.setMessage(new TranslatableText("speedrunigt.option.waiting_first_input").append(" : ").append(SpeedRunOptions.getOption(SpeedRunOptions.WAITING_FIRST_INPUT) ? ScreenTexts.ON : ScreenTexts.OFF));
                })
        , new TranslatableText("speedrunigt.option.waiting_first_input.description"));
        SpeedRunOptions.init();

        timerResetKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "speedrunigt.controls.start_timer",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "speedrunigt.title.options"
        ));

        timerStopKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "speedrunigt.controls.stop_timer",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "speedrunigt.title.options"
        ));
    }

    public static void debug(Object obj) {
        System.out.println(obj);
    }
}
