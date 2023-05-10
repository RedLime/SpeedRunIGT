package com.redlimerl.speedrunigt.utils;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ButtonWidgetHelper {
    public static ButtonWidget create(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
        return new ButtonWidget(x, y, width, height, message, onPress);
    }
}
