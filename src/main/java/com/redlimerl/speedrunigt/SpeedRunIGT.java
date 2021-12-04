package com.redlimerl.speedrunigt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redlimerl.speedrunigt.option.SpeedRunCategoryScreen;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

@SuppressWarnings("ResultOfMethodCallIgnored")
public class SpeedRunIGT implements ClientModInitializer {

    public static final String MOD_ID = "speedrunigt";
    public static final Identifier BUTTON_ICON_TEXTURE = new Identifier(MOD_ID, "textures/gui/buttons.png");
    public static final TimerDrawer TIMER_DRAWER = new TimerDrawer(true);

    public static String DEBUG_DATA = "";

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path MAIN_PATH = FabricLoader.getInstance().getGameDir().resolve(MOD_ID);
    public static final Path WORLDS_PATH = FabricLoader.getInstance().getGameDir().resolve("saves");
    public static final Path TIMER_PATH = MAIN_PATH.resolve("worlds");
    static {
        MAIN_PATH.toFile().mkdirs();
        TIMER_PATH.toFile().mkdirs();
    }

    private static KeyBinding timerStartKeyBinding;
    private static KeyBinding timerStopKeyBinding;

    @Override
    public void onInitializeClient() {
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.timer_position"), (ButtonWidget button) -> MinecraftClient.getInstance().setScreen(new TimerCustomizeScreen(screen)))
        );
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.timer_category"), (ButtonWidget button) -> MinecraftClient.getInstance().setScreen(new SpeedRunCategoryScreen(screen)))
        );
        SpeedRunOptions.addOptionButton(screen -> new ButtonWidget(0, 0, 150, 20, new TranslatableText("speedrunigt.option.timer_position.toggle_timer").append(" : ").append(TIMER_DRAWER.isToggle() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
            TIMER_DRAWER.setToggle(!TIMER_DRAWER.isToggle());
            SpeedRunOptions.setOption(SpeedRunOptions.TOGGLE_TIMER, TIMER_DRAWER.isToggle());
            button.setMessage(new TranslatableText("speedrunigt.option.timer_position.toggle_timer").append(" : ").append(TIMER_DRAWER.isToggle() ? ScreenTexts.ON : ScreenTexts.OFF));
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

        timerStartKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (timerStartKeyBinding.wasPressed()) {
                InGameTimer timer = InGameTimer.getInstance();
                if (timer.getCategory() == RunCategory.CUSTOM && timer.isResettable()) {
                    InGameTimer.reset();
                }
            }
            while (timerStopKeyBinding.wasPressed()) {
                InGameTimer timer = InGameTimer.getInstance();
                if (timer.getCategory() == RunCategory.CUSTOM && timer.isStarted()) {
                    InGameTimer.complete();
                }
            }
        });
    }

    public static void debug(Object obj) {
        System.out.println(obj);
    }
}
