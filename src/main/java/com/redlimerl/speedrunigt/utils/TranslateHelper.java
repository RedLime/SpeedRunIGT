package com.redlimerl.speedrunigt.utils;

import net.minecraft.client.MinecraftClient;

public class TranslateHelper {
    public static void reload() {
        MinecraftClient.getInstance().reloadResources();
    }
}
