package com.redlimerl.speedrunigt.version;

import net.minecraft.client.gui.widget.SliderWidget;

public abstract class CustomSliderWidget extends SliderWidget {
    protected CustomSliderWidget(int x, int y, int width, int height, double progress) {
        super(x, y, width, height, progress);
        updateMessage();
    }
}
