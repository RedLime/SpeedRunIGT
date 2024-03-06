package com.redlimerl.speedrunigt.gui;

import net.minecraft.client.Minecraft;
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
        this.width = width;
        this.height = height;
        this.message = onSlide.updateMessage();
        this.progress = f;
        this.onChange = onSlide;
    }

    public float getSliderValue() {
        return this.progress;
    }

    public int getYImage(boolean isHovered) {
        return 0;
    }

    protected void method_893(Minecraft client, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.progress = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
                if (this.progress < 0) {
                    this.progress = 0;
                }

                if (this.progress > 1) {
                    this.progress = 1;
                }

                this.onChange.applyValue(this.getSliderValue());
                this.message = onChange.updateMessage();
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexture(this.x + (int)(this.progress * (float)(this.width - 8)), this.y, 0, 66, 4, 20);
            this.drawTexture(this.x + (int)(this.progress * (float)(this.width - 8)) + 4, this.y, 196, 66, 4, 20);
        }
    }

    public boolean method_894(Minecraft client, int mouseX, int mouseY) {
        if (super.method_894(client, mouseX, mouseY)) {
            this.progress = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
            if (this.progress < 0.0F) {
                this.progress = 0.0F;
            }

            if (this.progress > 1.0F) {
                this.progress = 1.0F;
            }

            this.message = onChange.updateMessage();
            this.onChange.applyValue(this.getSliderValue());
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }
}
