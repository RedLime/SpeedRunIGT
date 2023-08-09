package com.redlimerl.speedrunigt;

import com.redlimerl.speedrunigt.instance.GameInstance;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class SpeedRunIGTClientInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SpeedRunIGTConfig.config = new SpeedRunIGTConfig(false);
        GameInstance.createInstance(FabricLoader.getInstance().getGameDir());
    }
}
