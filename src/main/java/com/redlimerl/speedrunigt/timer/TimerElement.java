package com.redlimerl.speedrunigt.timer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDisplayAlign;
import com.redlimerl.speedrunigt.timer.TimerDrawer.Position;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.math.MathHelper;

public class TimerElement {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private final TextRenderer textRenderer;
    private final Position position = new Position(0, 0);
    private final Position scaledPosition = new Position(0, 0);
    private float scale = 1;
    private int textWidth = 0;
    private String text;
    private Integer color;
    private TimerDecoration decoration;
    private float fontHeight = 8;

    public TimerElement(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public void init(float xPos, float yPos, float scale, String text, Integer color, TimerDecoration decoration, TimerDisplayAlign displayAlign, float fontHeight) {
        this.scale = scale;
        this.text = text;
        this.color = color;
        this.decoration = decoration;
        this.fontHeight = fontHeight;
        int scaledWindowWidth = client.getWindow().getScaledWidth();
        int scaledWindowHeight = client.getWindow().getScaledHeight();

        int translateX = (int) (xPos * scaledWindowWidth);
        int translateY = (int) (yPos * scaledWindowHeight);

        this.position.setX(translateX);
        this.position.setY(translateY);
        this.scaledPosition.setX(Math.round(translateX / this.scale));
        this.scaledPosition.setY(Math.round(translateY / this.scale));

        this.textWidth = this.textRenderer.getStringWidth(text);

        if (displayAlign != TimerDisplayAlign.LEFT) {
            if (displayAlign == TimerDisplayAlign.RIGHT || (displayAlign == TimerDisplayAlign.AUTO && this.getScaledTextWidth() + this.position.getX() > scaledWindowWidth)) {
                this.scaledPosition.setX(this.scaledPosition.getX() - Math.round((this.getScaledTextWidth() - 1) / scale));
                this.position.setX(this.position.getX() - this.getScaledTextWidth());
            }
            if (displayAlign == TimerDisplayAlign.CENTER) {
                this.scaledPosition.setX(this.scaledPosition.getX() - Math.round((this.getScaledTextWidth() - 1) / scale / 2));
                this.position.setX(this.position.getX() - (this.getScaledTextWidth() / 2));
            }
        }

        // Fix vertical height
        if (this.getScaledTextHeight() + this.position.getY() > scaledWindowHeight) {
            this.scaledPosition.setY(this.scaledPosition.getY() - MathHelper.floor(this.getScaledTextHeight() / scale));
            this.position.setY(this.position.getY() - this.getScaledTextWidth());
        }
    }

    public void draw(boolean doTranslate) {
        RenderSystem.pushMatrix();
        if (doTranslate) RenderSystem.translatef(0, 0, 1);
        RenderSystem.scalef(scale, scale, 1f);
        drawOutLine(this.textRenderer, scaledPosition.getX(), scaledPosition.getY(), text, color, decoration);
        RenderSystem.popMatrix();
    }

    private static void drawOutLine(TextRenderer textRenderer, int x, int y, String text, Integer color, TimerDecoration decoration) {
        if (decoration == TimerDecoration.OUTLINE) {
            textRenderer.draw(text, (float)x + 1, (float)y + 1, 0);
            textRenderer.draw(text, (float)x + 1, (float)y, 0);
            textRenderer.draw(text, (float)x + 1, (float)y - 1, 0);
            textRenderer.draw(text, (float)x, (float)y - 1, 0);
            textRenderer.draw(text, (float)x, (float)y + 1, 0);
            textRenderer.draw(text, (float)x - 1, (float)y + 1, 0);
            textRenderer.draw(text, (float)x - 1, (float)y, 0);
            textRenderer.draw(text, (float)x - 1, (float)y - 1, 0);
        } else if (decoration == TimerDecoration.SHADOW) {
            textRenderer.draw(text, (float)x + 1, (float)y + 1, -12566464);
        }
        textRenderer.draw(text, (float)x, (float)y, color);
    }

    public Position getPosition() {
        return position;
    }

    public int getScaledTextWidth() {
        return Math.round(textWidth * scale);
    }

    public int getScaledTextHeight() {
        return Math.round(fontHeight * scale);
    }
}