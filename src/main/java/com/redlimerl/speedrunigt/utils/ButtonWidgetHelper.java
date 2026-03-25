package com.redlimerl.speedrunigt.utils;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class ButtonWidgetHelper {
    public static Button create(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
        return Button.builder(message, onPress).bounds(x, y, width, height).build();
    }

    public static Button create(int x, int y, int width, int height, Component message, Button.OnPress onPress, Tooltip tooltipSupplier) {
        return Button.builder(message, onPress).bounds(x, y, width, height).tooltip(tooltipSupplier).build();
    }
}
