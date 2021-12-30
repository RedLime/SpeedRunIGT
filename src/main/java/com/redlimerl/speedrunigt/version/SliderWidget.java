package com.redlimerl.speedrunigt.version;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderWidget extends ButtonWidget {
    private final Consumer<Float> onChange;
    private final Supplier<String> text;
    private float value;
    public boolean dragging;
    private final float min;
    private final float max;

    public SliderWidget(Consumer<Float> onChange, int id, int x, int y, Supplier<String> text, float min, float max, float value) {
        super(id, x, y, 150, 20, "");
        this.onChange = onChange;
        this.text = text;
        this.value = value;
        this.min = min;
        this.max = max;
        this.message = text.get();
    }

    public int getYImage(boolean isHovered) {
        return 0;
    }

    protected void renderBg(MinecraftClient client, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.value = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
                if (this.value < min) {
                    this.value = min;
                }

                if (this.value > max) {
                    this.value = max;
                }

                this.onChange.accept(this.value);
                this.message = text.get();
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexture(this.x + (int)(((this.value - this.min) / (this.max - this.min)) * (float)(this.width - 8)), this.y, 0, 66, 4, 20);
            this.drawTexture(this.x + (int)(((this.value - this.min) / (this.max - this.min)) * (float)(this.width - 8)) + 4, this.y, 196, 66, 4, 20);
        }
    }

    public boolean isMouseOver(MinecraftClient client, int mouseX, int mouseY) {
        if (super.isMouseOver(client, mouseX, mouseY)) {
            this.value = (float)(mouseX - (this.x + 4)) / (float)(this.width - 8);
            if (this.value < 0.0F) {
                this.value = 0.0F;
            }

            if (this.value > 1.0F) {
                this.value = 1.0F;
            }

            this.message = text.get();
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
