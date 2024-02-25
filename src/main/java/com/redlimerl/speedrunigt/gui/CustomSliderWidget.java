package com.redlimerl.speedrunigt.gui;

import net.minecraft.class_0_692;
import net.minecraft.class_0_705;

public class CustomSliderWidget extends class_0_692 {

    public interface SliderWorker {
        String updateMessage();
        void applyValue(float value);
    }

    public CustomSliderWidget(int x, int y, int width, int height, float f, SliderWorker onSlide) {
        super(new class_0_705.class_0_707() {
            @Override
            public void method_0_2590(int id, boolean value) {

            }

            @Override
            public void method_0_2588(int id, float value) {
                onSlide.applyValue(value);
            }

            @Override
            public void method_0_2589(int id, String text) {

            }
        }, 0, x, y, "", 0f, 1f, f, (i, s, v) -> onSlide.updateMessage());
        this.field_2071 = width;
        this.field_2070 = height;
        this.field_2074 = onSlide.updateMessage();
    }
}
