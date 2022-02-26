package com.redlimerl.speedrunigt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.api.SpeedRunIGTApi;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunIGTInfoScreen;
import com.redlimerl.speedrunigt.impl.CategoryRegistryImpl;
import com.redlimerl.speedrunigt.impl.OptionButtonsImpl;
import com.redlimerl.speedrunigt.impl.SplitTypeRegistryImpl;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.timer.TimerRecord;
import com.redlimerl.speedrunigt.timer.running.RunCategory;
import com.redlimerl.speedrunigt.timer.running.RunSplitType;
import com.redlimerl.speedrunigt.utils.KeyBindingRegistry;
import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.options.KeyBinding;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class SpeedRunIGT implements ClientModInitializer {

    public static final String MOD_ID = "speedrunigt";
    public static TimerDrawer TIMER_DRAWER = new TimerDrawer(true);
    private static boolean isInitialized = false;
    public static boolean isInitialized() { return isInitialized; }

    public static String DEBUG_DATA = "";
    public static String MOD_VERSION;
    public static Long LATEST_PLAYED_SEED = 0L;
    public static boolean LATEST_IS_SSG = false;
    public static boolean LATEST_IS_FSG = false;

    public static final Gson GSON = new GsonBuilder().create();
    public static final Path WORLDS_PATH = FabricLoader.getInstance().getGameDir().resolve("saves");
    public static final Path FONT_PATH = getGlobalPath().resolve("fonts");

    public static Path getMainPath() {
        return FabricLoader.getInstance().getGameDir().resolve(MOD_ID);
    }
    public static Path getGlobalPath() { return new File(System.getProperty("user.home").replace("\\", "/"), SpeedRunIGT.MOD_ID).toPath(); }

    public static final ArrayList<ModContainer> API_PROVIDERS = new ArrayList<>();

    static {
        getMainPath().toFile().mkdirs();
        getGlobalPath().toFile().mkdirs();
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

        // init default option buttons
        SpeedRunOption.addOptionButtonFactories(new OptionButtonsImpl().createOptionButtons().toArray(new OptionButtonFactory[0]));
        // init default categories
        new CategoryRegistryImpl().registerCategories().forEach(RunCategory::registerCategory);
        // init default split types
        new SplitTypeRegistryImpl().registerSplitTypes().forEach(RunSplitType::registrySplitType);


        // Registry API's
        for (EntrypointContainer<SpeedRunIGTApi> entryPoint : FabricLoader.getInstance().getEntrypointContainers("speedrunigt", SpeedRunIGTApi.class)) {
            SpeedRunIGTApi api = entryPoint.getEntrypoint();

            // Registry single option button
            OptionButtonFactory singleFactory = api.createOptionButton();
            if (singleFactory != null) SpeedRunOption.addOptionButtonFactories(singleFactory);

            // Registry multiple option buttons
            Collection<OptionButtonFactory> multipleFactory = api.createOptionButtons();
            if (multipleFactory != null) SpeedRunOption.addOptionButtonFactories(multipleFactory.toArray(new OptionButtonFactory[0]));

            // Registry single category
            RunCategory singleCategory = api.registerCategory();
            if (singleCategory != null) RunCategory.registerCategory(singleCategory);

            // Registry multiple categories
            Collection<RunCategory> multipleCategories = api.registerCategories();
            if (multipleCategories != null) multipleCategories.forEach(RunCategory::registerCategory);

            // Registry single split type
            RunSplitType runSplitType = api.registerSplitType();
            if (runSplitType != null) RunSplitType.registrySplitType(runSplitType);

            // Registry multiple split types
            Collection<RunSplitType> runSplitTypes = api.registerSplitTypes();
            if (runSplitTypes != null) runSplitTypes.forEach(RunSplitType::registrySplitType);

            API_PROVIDERS.add(entryPoint.getProvider());
        }

        // Options initialize
        SpeedRunOption.init();

        // Translate initialize
        try {
            TranslateHelper.init();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        // Key Bindings initialize
        timerResetKeyBinding = KeyBindingRegistry.registerKeyBinding(new KeyBinding(
                "speedrunigt.controls.start_timer",
                22,
                "speedrunigt.title.options"
        ));
        timerStopKeyBinding = KeyBindingRegistry.registerKeyBinding(new KeyBinding(
                "speedrunigt.controls.stop_timer",
                23,
                "speedrunigt.title.options"
        ));

        // End initializing
        isInitialized = true;

        // Update checking
        SpeedRunIGTInfoScreen.checkUpdate();
    }

    private static final Logger LOGGER = LogManager.getLogger("SpeedRunIGT");
    public static void debug(Object obj) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) LOGGER.info(obj);
    }
    public static void error(Object obj) { LOGGER.error(obj); }
}
//Void was here :)
