package com.redlimerl.speedrunigt;

import com.redlimerl.speedrunigt.instance.GameInstance;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class SpeedRunIGTDedicatedServerInitializer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        SpeedRunIGTConfig.config = new SpeedRunIGTConfig(true);
        GameInstance.createInstance(FabricLoader.getInstance().getGameDir());
    }
}
