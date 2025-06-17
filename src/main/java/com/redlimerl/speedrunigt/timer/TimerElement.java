package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDisplayAlign;
import com.redlimerl.speedrunigt.timer.TimerDrawer.Position;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class TimerElement {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private final Position position = new Position(0, 0);
    private final Position scaledPosition = new Position(0, 0);
    private float scale = 1;
    private int textWidth = 0;
    private MutableText text;
    private Integer color;
    private TimerDecoration decoration;
    private float fontHeight = 8;

    public void init(float xPos, float yPos, float scale, MutableText text, Integer color, TimerDecoration decoration, TimerDisplayAlign displayAlign, float fontHeight) {
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

        this.textWidth = client.textRenderer.getWidth(text);

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
        if (getScaledTextHeight() + this.position.getY() > scaledWindowHeight) {
            this.scaledPosition.setY(this.scaledPosition.getY() - MathHelper.floor(getScaledTextHeight() / scale));
            this.position.setY(this.position.getY() - getScaledTextWidth());
        }
    }

    public void draw(DrawContext drawContext, boolean doTranslate) {
        drawContext.getMatrices().pushMatrix();
//        if (doTranslate) drawContext.getMatrices().transform(new Vector3f(0, 0, 1));
        drawContext.getMatrices().scale(scale, scale);
        drawOutLine(client.textRenderer, drawContext, scaledPosition.getX(), scaledPosition.getY(), text, color, decoration);
        drawContext.getMatrices().popMatrix();
    }

    private static void drawOutLine(TextRenderer textRenderer, DrawContext drawContext, int x, int y, MutableText text, Integer color, TimerDecoration decoration) {
        if (decoration == TimerDecoration.OUTLINE) {
            drawContext.drawText(textRenderer, text, x + 1, y + 1, Colors.BLACK, false);
            drawContext.drawText(textRenderer, text, x + 1, y, Colors.BLACK, false);
            drawContext.drawText(textRenderer, text, x + 1, y - 1, Colors.BLACK, false);
            drawContext.drawText(textRenderer, text, x, y - 1, Colors.BLACK, false);
            drawContext.drawText(textRenderer, text, x, y + 1, Colors.BLACK, false);
            drawContext.drawText(textRenderer, text, x - 1, y + 1, Colors.BLACK, false);
            drawContext.drawText(textRenderer, text, x - 1, y, Colors.BLACK, false);
            drawContext.drawText(textRenderer, text, x - 1, y - 1, Colors.BLACK, false);
        } else if (decoration == TimerDecoration.SHADOW) {
            drawContext.drawText(textRenderer, text, x + 1, y + 1, Colors.DARK_GRAY, false);
        }
        drawContext.drawText(textRenderer, text, x, y, ColorHelper.getArgb(ColorHelper.getRed(color), ColorHelper.getGreen(color), ColorHelper.getBlue(color)), false);
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
