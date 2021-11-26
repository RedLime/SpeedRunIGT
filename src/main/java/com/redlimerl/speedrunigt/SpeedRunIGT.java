package com.redlimerl.speedrunigt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redlimerl.speedrunigt.option.SpeedRunCategoryScreen;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.KeyBinding;
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

    private static KeyBinding keyBinding;

    @Override
    public void onInitializeClient() {
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.timer_position"), (ButtonWidget button) -> MinecraftClient.getInstance().openScreen(new TimerCustomizeScreen(screen)))
        );
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.timer_category"), (ButtonWidget button) -> MinecraftClient.getInstance().openScreen(new SpeedRunCategoryScreen(screen)))
        );
        SpeedRunOptions.init();

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.examplemod.spook", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_U, // The keycode of the key
                "category.examplemod.test" // The translation key of the keybinding's category.
        ));
    }

    public static void debug(Object obj) {
        System.out.println(obj);
    }
}
