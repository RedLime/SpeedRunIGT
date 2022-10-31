package com.redlimerl.speedrunigt;

import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.api.SpeedRunIGTApi;
import com.redlimerl.speedrunigt.impl.OptionButtonsImpl;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.therun.TheRunKeyHelper;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.utils.FontUtils;
import com.redlimerl.speedrunigt.utils.KeyBindingRegistry;
import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;

public class SpeedRunIGTClient implements ClientModInitializer {
    public static TimerDrawer TIMER_DRAWER = new TimerDrawer(true);

    public static KeyBinding timerResetKeyBinding;
    public static KeyBinding timerStopKeyBinding;
    public static boolean isInitialized = false;

    @Override
    public void onInitializeClient() {
        // init default option buttons
        SpeedRunOption.addOptionButtonFactories(new OptionButtonsImpl().createOptionButtons().toArray(new OptionButtonFactory[0]));

        // Registry API's
        for (EntrypointContainer<SpeedRunIGTApi> entryPoint : FabricLoader.getInstance().getEntrypointContainers("speedrunigt", SpeedRunIGTApi.class)) {
            SpeedRunIGTApi api = entryPoint.getEntrypoint();

            // Registry single option button
            OptionButtonFactory singleFactory = api.createOptionButton();
            if (singleFactory != null) SpeedRunOption.addOptionButtonFactories(singleFactory);

            // Registry multiple option buttons
            Collection<OptionButtonFactory> multipleFactory = api.createOptionButtons();
            if (multipleFactory != null) SpeedRunOption.addOptionButtonFactories(multipleFactory.toArray(new OptionButtonFactory[0]));

            SpeedRunIGT.API_PROVIDERS.add(entryPoint.getProvider());
        }

        // End initializing
        isInitialized = true;

        // Translate initialize
        try {
            TranslateHelper.init();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        // Key Bindings initialize
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

        // Add default fonts
        FontUtils.copyDefaultFonts();

        TheRunKeyHelper.load();

        SpeedRunIGT.IS_CLIENT_SIDE = true;
    }
}
