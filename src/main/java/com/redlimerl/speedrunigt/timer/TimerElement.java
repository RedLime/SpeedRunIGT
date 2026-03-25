package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDisplayAlign;
import com.redlimerl.speedrunigt.timer.TimerDrawer.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

public class TimerElement {
    private static final Minecraft client = Minecraft.getInstance();

    private final Position position = new Position(0, 0);
    private final Position scaledPosition = new Position(0, 0);
    private float scale = 1;
    private int textWidth = 0;
    private MutableComponent text;
    private Integer color;
    private TimerDecoration decoration;
    private float fontHeight = 8;

    public void init(float xPos, float yPos, float scale, MutableComponent text, Integer color, TimerDecoration decoration, TimerDisplayAlign displayAlign, float fontHeight) {
        this.scale = scale;
        this.text = text;
        this.color = color;
        this.decoration = decoration;
        this.fontHeight = fontHeight;
        int scaledWindowWidth = client.getWindow().getGuiScaledWidth();
        int scaledWindowHeight = client.getWindow().getGuiScaledHeight();

        int translateX = (int) (xPos * scaledWindowWidth);
        int translateY = (int) (yPos * scaledWindowHeight);

        this.position.setX(translateX);
        this.position.setY(translateY);
        this.scaledPosition.setX(Math.round(translateX / this.scale));
        this.scaledPosition.setY(Math.round(translateY / this.scale));

        this.textWidth = client.font.width(text);

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
            this.scaledPosition.setY(this.scaledPosition.getY() - Mth.floor(getScaledTextHeight() / scale));
            this.position.setY(this.position.getY() - getScaledTextWidth());
        }
    }

    public void draw(@NonNull GuiGraphicsExtractor graphics, boolean doTranslate) {
        graphics.pose().pushMatrix();
//        if (doTranslate) drawContext.getMatrices().transform(new Vector3f(0, 0, 1));
        graphics.pose().scale(scale, scale);
        drawOutLine(client.font, graphics, scaledPosition.getX(), scaledPosition.getY(), text, color, decoration);
        graphics.pose().popMatrix();
    }

    private static void drawOutLine(Font textRenderer, GuiGraphicsExtractor graphics, int x, int y, MutableComponent text, Integer color, TimerDecoration decoration) {
        if (decoration == TimerDecoration.OUTLINE) {
            graphics.text(textRenderer, text, x + 1, y + 1, CommonColors.BLACK, false);
            graphics.text(textRenderer, text, x + 1, y, CommonColors.BLACK, false);
            graphics.text(textRenderer, text, x + 1, y - 1, CommonColors.BLACK, false);
            graphics.text(textRenderer, text, x, y - 1, CommonColors.BLACK, false);
            graphics.text(textRenderer, text, x, y + 1, CommonColors.BLACK, false);
            graphics.text(textRenderer, text, x - 1, y + 1, CommonColors.BLACK, false);
            graphics.text(textRenderer, text, x - 1, y, CommonColors.BLACK, false);
            graphics.text(textRenderer, text, x - 1, y - 1, CommonColors.BLACK, false);
        } else if (decoration == TimerDecoration.SHADOW) {
            graphics.text(textRenderer, text, x + 1, y + 1, CommonColors.DARK_GRAY, false);
        }
        graphics.text(textRenderer, text, x, y, ARGB.color(ARGB.red(color), ARGB.green(color), ARGB.blue(color)), false);
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
