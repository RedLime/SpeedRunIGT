package com.redlimerl.speedrunigt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.api.SpeedRunIGTApi;
import com.redlimerl.speedrunigt.impl.OptionButtonsImpl;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.timer.TimerSplit;
import com.redlimerl.speedrunigt.utils.FontIdentifier;
import com.redlimerl.speedrunigt.utils.KeyBindingRegistry;
import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class SpeedRunIGT implements ClientModInitializer {

    public static final String MOD_ID = "speedrunigt";
    public static TimerDrawer TIMER_DRAWER = new TimerDrawer(true);
    private static boolean isInitialized = false;
    public static boolean isInitialized() { return isInitialized; }

    public static String DEBUG_DATA = "";
    public static String MOD_VERSION;
    public static HashMap<Identifier, FontIdentifier> FONT_MAPS = new HashMap<>();
    public static Long LATEST_PLAYED_SEED = 0L;
    public static boolean LATEST_IS_SSG = false;
    public static boolean LATEST_IS_FSG = false;

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path WORLDS_PATH = FabricLoader.getInstance().getGameDir().resolve("saves");
    public static final Path FONT_PATH = getMainPath().resolve("fonts");

    public static Path getMainPath() {
        return FabricLoader.getInstance().getGameDir().resolve(MOD_ID);
    }


    static {
        getMainPath().toFile().mkdirs();
        FONT_PATH.toFile().mkdirs();

        //Delete all old timer data
        File oldWorlds = getMainPath().resolve("worlds").toFile();
        if (oldWorlds.exists()) {
            try {
                FileUtils.deleteDirectory(oldWorlds);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static KeyBinding timerResetKeyBinding;
    public static KeyBinding timerStopKeyBinding;

    @Override
    public void onInitializeClient() {
        MOD_VERSION = (FabricLoader.getInstance().getModContainer(SpeedRunIGT.MOD_ID).isPresent()
                        ? FabricLoader.getInstance().getModContainer(SpeedRunIGT.MOD_ID).get().getMetadata().getVersion().getFriendlyString() : "Unknown+Unknown");

        //init option buttons
        SpeedRunOption.addOptionButtonFactories(new OptionButtonsImpl().createOptionButtons().toArray(new OptionButtonFactory[0]));

        for (EntrypointContainer<SpeedRunIGTApi> entryPoint : FabricLoader.getInstance().getEntrypointContainers("speedrunigt", SpeedRunIGTApi.class)) {
            SpeedRunIGTApi api = entryPoint.getEntrypoint();

            OptionButtonFactory singleFactory = api.createOptionButton();
            if (singleFactory != null) SpeedRunOption.addOptionButtonFactories(singleFactory);

            SpeedRunOption.addOptionButtonFactories(api.createOptionButtons().toArray(new OptionButtonFactory[0]));
        }

        SpeedRunOption.init();

        timerResetKeyBinding = KeyBindingRegistry.registerKeyBinding(new KeyBinding(
                "speedrunigt.controls.start_timer",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "speedrunigt.title.options"
        ));

        timerStopKeyBinding = KeyBindingRegistry.registerKeyBinding(new KeyBinding(
                "speedrunigt.controls.stop_timer",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "speedrunigt.title.options"
        ));

        TimerSplit.load();

        try {
            TranslateHelper.init();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        isInitialized = true;
    }

    private static final Logger LOGGER = LogManager.getLogger("SpeedRunIGT");
    public static void debug(Object obj) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) LOGGER.info(obj);
    }
    public static void error(Object obj) { LOGGER.error(obj); }
}
//Void was here :)
