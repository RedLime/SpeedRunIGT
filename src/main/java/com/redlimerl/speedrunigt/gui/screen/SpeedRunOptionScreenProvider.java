package com.redlimerl.speedrunigt.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;
import org.mcsr.speedrunapi.config.api.SpeedrunConfigScreenProvider;

@SuppressWarnings("unused")
public class SpeedRunOptionScreenProvider implements SpeedrunConfigScreenProvider {
    @Override
    public @NotNull Screen createConfigScreen(Screen parent) {
        return new SpeedRunOptionScreen(parent);
    }
}
