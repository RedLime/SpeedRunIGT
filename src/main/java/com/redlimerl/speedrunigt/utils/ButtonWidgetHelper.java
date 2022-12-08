package com.redlimerl.speedrunigt.utils;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ButtonWidgetHelper {
    public static ButtonWidget create(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
        return ButtonWidget.builder(message, onPress).dimensions(x, y, width, height).build();
    }

    public static ButtonWidget create(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress, Tooltip tooltipSupplier) {
        return ButtonWidget.builder(message, onPress).dimensions(x, y, width, height).tooltip(tooltipSupplier).build();
    }
}
