package com.redlimerl.speedrunigt;

import com.redlimerl.speedrunigt.config.Options;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpeedRunIGT implements ClientModInitializer {

    public static final String MOD_ID = "speedrunigt";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final Identifier BUTTON_ICON_TEXTURE = new Identifier(MOD_ID, "textures/gui/buttons.png");

    @Override
    public void onInitializeClient() {
        Options.INSTANCE.init();
    }
}
