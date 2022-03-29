package com.redlimerl.speedrunigt.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.lwjgl.opengl.GL11;

public class CustomSliderWidget extends ButtonWidget {

    private float progress;
    public boolean dragging;
    private final SliderWorker onChange;

    public interface SliderWorker {
        String updateMessage();
        void applyValue(float value);
    }

    public CustomSliderWidget(int x, int y, int width, int height, float f, SliderWorker onSlide) {
        super(99999, x, y, width, height, "");
        this.field_22508 = width;
        this.field_22509 = height;
        this.field_22510 = onSlide.updateMessage();
        this.progress = f;
        this.onChange = onSlide;
    }

    public float getSliderValue() {
        return this.progress;
    }

    protected int method_21889(boolean bl) {
        return 0;
    }

    protected void method_21892(MinecraftClient minecraftClient, int i, int j) {
        if (this.field_22512) {
            if (this.dragging) {
                this.progress = (float)(i - (this.x + 4)) / (float)(this.field_22508 - 8);
                if (this.progress < 0.0F) {
                    this.progress = 0.0F;
                }

                if (this.progress > 1.0F) {
                    this.progress = 1.0F;
                }

                this.onChange.applyValue(this.getSliderValue());
                this.field_22510 = onChange.updateMessage();
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.method_21883(this.x + (int)(this.progress * (float)(this.field_22508 - 8)), this.y, 0, 66, 4, 20);
            this.method_21883(this.x + (int)(this.progress * (float)(this.field_22508 - 8)) + 4, this.y, 196, 66, 4, 20);
        }
    }

    public boolean method_21893(MinecraftClient minecraftClient, int i, int j) {
        if (super.method_21893(minecraftClient, i, j)) {
            this.progress = (float)(i - (this.x + 4)) / (float)(this.field_22508 - 8);
            if (this.progress < 0.0F) {
                this.progress = 0.0F;
            }

            if (this.progress > 1.0F) {
                this.progress = 1.0F;
            }

            this.onChange.applyValue(this.getSliderValue());
            this.field_22510 = onChange.updateMessage();
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    public void method_21886(int i, int j) {
        this.dragging = false;
    }
}
