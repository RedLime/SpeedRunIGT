package com.redlimerl.speedrunigt.gui;

import net.minecraft.client.gui.widget.PagedEntryListWidget;
import net.minecraft.client.gui.widget.SliderWidget;

public class CustomSliderWidget extends SliderWidget {

    public interface SliderWorker {
        String updateMessage();
        void applyValue(float value);
    }

    public CustomSliderWidget(int x, int y, int width, int height, float f, SliderWorker onSlide) {
        super(new PagedEntryListWidget.Listener() {
            @Override
            public void setBooleanValue(int id, boolean value) {

            }

            @Override
            public void setFloatValue(int id, float value) {
                onSlide.applyValue(value);
            }

            @Override
            public void setStringValue(int id, String text) {

            }
        }, 0, x, y, "", 0f, 1f, f, (i, s, v) -> onSlide.updateMessage());
        this.width = width;
        this.height = height;
        this.message = onSlide.updateMessage();
    }
}
