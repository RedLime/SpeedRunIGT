package com.redlimerl.speedrunigt.utils;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ButtonWidgetHelper {
    public static ButtonWidget create(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress) {
        return ButtonWidget.method_46430(message, onPress).method_46434(x, y, width, height).method_46431();
    }

    public static ButtonWidget create(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress, ButtonWidget.TooltipSupplier tooltipSupplier) {
        return ButtonWidget.method_46430(message, onPress).method_46434(x, y, width, height).method_46436(tooltipSupplier).method_46431();
    }
}
