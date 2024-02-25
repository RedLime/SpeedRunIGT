package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.timer.TimerDrawer.Position;
import net.minecraft.class_0_681;
import net.minecraft.class_0_686;
import net.minecraft.class_1015;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class TimerElement {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private final class_0_681 textRenderer = client.field_1772;
    private final Position position = new Position(0, 0);
    private final Position scaledPosition = new Position(0, 0);
    private float scale = 1;
    private int textWidth = 0;
    private String text;
    private Integer color;
    private TimerDecoration decoration;
    private float fontHeight = 8;
    private final class_0_686 window = new class_0_686(client);

    public void init(float xPos, float yPos, float scale, String text, Integer color, TimerDecoration decoration, float fontHeight) {
        this.scale = scale;
        this.text = text;
        this.color = color;
        this.decoration = decoration;
        this.fontHeight = fontHeight;
        double scaledWindowWidth = window.method_0_2461();
        double scaledWindowHeight = window.method_0_2462();

        int translateX = (int) (xPos * scaledWindowWidth);
        int translateY = (int) (yPos * scaledWindowHeight);

        this.position.setX(translateX);
        this.position.setY(translateY);
        this.scaledPosition.setX(Math.round(translateX / this.scale));
        this.scaledPosition.setY(Math.round(translateY / this.scale));

        this.textWidth = this.textRenderer.method_0_2381(text);

        //가로 화면 밖으로 나갈 시 재조정
        if (getScaledTextWidth() + this.position.getX() > scaledWindowWidth) {
            this.scaledPosition.setX(this.scaledPosition.getX() - Math.round((getScaledTextWidth() - 1) / scale));
            this.position.setX(this.position.getX() - getScaledTextWidth());
        }
        //세로 화면 밖으로 나갈 시 재조정
        if (getScaledTextHeight() + this.position.getY() > scaledWindowHeight) {
            this.scaledPosition.setY(this.scaledPosition.getY() - MathHelper.floor(getScaledTextHeight() / scale));
            this.position.setY(this.position.getY() - getScaledTextWidth());
        }
    }

    public void draw(boolean doTranslate) {
        class_1015.method_4461();
        if (doTranslate) class_1015.method_4348(0, 0, 1);
        class_1015.method_4384(scale, scale, 1.0F);
        drawOutLine(this.textRenderer, scaledPosition.getX(), scaledPosition.getY(), text, color, decoration);
        class_1015.method_4350();
    }

    private static void drawOutLine(class_0_681 textRenderer, int x, int y, String text, Integer color, TimerDecoration decoration) {
        if (decoration == TimerDecoration.OUTLINE) {
            textRenderer.method_0_2385(text, x + 1, y + 1, 0);
            textRenderer.method_0_2385(text, x + 1, y, 0);
            textRenderer.method_0_2385(text, x + 1, y - 1, 0);
            textRenderer.method_0_2385(text, x, y - 1, 0);
            textRenderer.method_0_2385(text, x, y + 1, 0);
            textRenderer.method_0_2385(text, x - 1, y + 1, 0);
            textRenderer.method_0_2385(text, x - 1, y, 0);
            textRenderer.method_0_2385(text, x - 1, y - 1, 0);
        } else if (decoration == TimerDecoration.SHADOW) {
            textRenderer.method_0_2385(text, x + 1, y + 1, -12566464);
        }
        textRenderer.method_0_2385(text, x, y, color);
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