package com.redlimerl.speedrunigt;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redlimerl.speedrunigt.api.CategoryConditionRegisterHelper;
import com.redlimerl.speedrunigt.api.SpeedRunIGTApi;
import com.redlimerl.speedrunigt.impl.CategoryRegistryImpl;
import com.redlimerl.speedrunigt.impl.ConditionsRegistryImpl;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.timer.category.CustomCategoryManager;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.packet.TimerPackets;
import com.redlimerl.speedrunigt.utils.FilesHelper;
import com.redlimerl.speedrunigt.utils.FontIdentifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class SpeedRunIGT implements ModInitializer {

    public static final String MOD_ID = "speedrunigt";
    private static boolean isInitialized = false;
    public static boolean isInitialized() { return isInitialized; }
    public static boolean IS_CLIENT_SIDE = false;
    public static boolean IS_DEBUG_MODE = FabricLoader.getInstance().isDevelopmentEnvironment();
    public static MinecraftServer DEDICATED_SERVER = null;

    public static String DEBUG_DATA = "";
    public static String MOD_VERSION;
    public static HashMap<Identifier, FontIdentifier> FONT_MAPS = new HashMap<>();

    public static final Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    public static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    public static final Path FONT_PATH = getGlobalPath().resolve("fonts");

    public static Path getMainPath() {
        return FabricLoader.getInstance().getGameDir().resolve(MOD_ID);
    }
    public static Path getRecordsPath() { return getGlobalPath().resolve("records"); }
    public static Path getGlobalPath() {
        String home = System.getProperty("user.home").replace("\\", "/");
        Path path = new File(home, SpeedRunIGT.MOD_ID).toPath();
        Path linuxPath = new File(home, "." + SpeedRunIGT.MOD_ID).toPath();
        if (Util.getOperatingSystem() == Util.OperatingSystem.LINUX) {
            // used to use ~/speedrunigt instead of ~/.speedrunigt on linux
            // if only the old directory exists we need to copy it over to the new one
            if (Files.exists(path) && !Files.exists(linuxPath)) {
                FilesHelper.recursiveCopy(path, linuxPath);
            }
            return linuxPath;
        } else {
            return path;
        }
    }

    public static final Set<ModContainer> API_PROVIDERS = Sets.newHashSet();

    static {
        getMainPath().toFile().mkdirs();
        getGlobalPath().toFile().mkdirs();
        getRecordsPath().toFile().mkdirs();
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

    @Override
    public void onInitialize() {
        MOD_VERSION = (FabricLoader.getInstance().getModContainer(SpeedRunIGT.MOD_ID).isPresent()
                        ? FabricLoader.getInstance().getModContainer(SpeedRunIGT.MOD_ID).get().getMetadata().getVersion().getFriendlyString() : "Unknown+Unknown");

        // init default categories
        new CategoryRegistryImpl().registerCategories().forEach(RunCategory::registerCategory);
        CategoryCondition.registerCondition(new ConditionsRegistryImpl().registerConditions());


        // Registry API's
        for (EntrypointContainer<SpeedRunIGTApi> entryPoint : FabricLoader.getInstance().getEntrypointContainers("speedrunigt", SpeedRunIGTApi.class)) {
            SpeedRunIGTApi api = entryPoint.getEntrypoint();

            // Registry single category
            RunCategory singleCategory = api.registerCategory();
            if (singleCategory != null) RunCategory.registerCategory(singleCategory);

            // Registry multiple categories
            Collection<RunCategory> multipleCategories = api.registerCategories();
            if (multipleCategories != null) multipleCategories.forEach(RunCategory::registerCategory);

            // Registry multiple conditions
            Map<String, CategoryConditionRegisterHelper> multipleConditions = api.registerConditions();
            if (multipleConditions != null) CategoryCondition.registerCondition(multipleConditions);

            API_PROVIDERS.add(entryPoint.getProvider());
        }

        // Load speedrun options
        SpeedRunOption.init();

        // Custom Json Category initialize
        CustomCategoryManager.init();

        // End initializing
        isInitialized = true;

        // Set properties
        System.setProperty("speedrunigt.version", MOD_VERSION.split("\\+")[0]);
        System.setProperty("speedrunigt.record", "");

        // Update checking
        SpeedRunIGTUpdateChecker.checkUpdate();

        // Initializing packets
        TimerPackets.init();
    }

    private static final Logger LOGGER = LogManager.getLogger("SpeedRunIGT");
    public static void debug(Object obj) {
        if (IS_DEBUG_MODE) LOGGER.info(obj);
    }
    public static void error(Object obj) { LOGGER.error(obj); }
}
//Void was here :)
