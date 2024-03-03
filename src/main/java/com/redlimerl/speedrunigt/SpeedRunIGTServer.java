package com.redlimerl.speedrunigt;

import com.redlimerl.speedrunigt.instance.GameInstance;
import net.fabricmc.api.DedicatedServerModInitializer;

public class SpeedRunIGTServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        GameInstance.createInstance();
    }
}
