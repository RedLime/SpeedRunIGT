package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.timer.TimerDrawer.Position;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * @author Void_X_Walker
 * @reason Backported to 1.8
 */
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

    public TimerElement(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public void init(float xPos, float yPos, float scale, String text, Integer color, TimerDecoration decoration) {
        this.scale = scale;
        this.text = text;
        this.color = color;
        this.decoration = decoration;
        int scaledWindowWidth = client.width;
        int scaledWindowHeight = client.height;

        int translateX = (int) (xPos * scaledWindowWidth);
        int translateY = (int) (yPos * scaledWindowHeight);

        this.position.setX(translateX);
        this.position.setY(translateY);
        this.scaledPosition.setX(Math.round(translateX / this.scale));
        this.scaledPosition.setY(Math.round(translateY / this.scale));

        this.textWidth = this.textRenderer.getStringWidth(text);

        if (getScaledTextWidth() + this.position.getX() > scaledWindowWidth) {
            this.scaledPosition.setX(this.scaledPosition.getX() - Math.round((getScaledTextWidth() - 1) / scale));
        }
        if (getScaledTextHeight() + this.position.getY() > scaledWindowHeight) {
            this.scaledPosition.setY(this.scaledPosition.getY() - MathHelper.floor((getScaledTextHeight() - 1) / scale));
        }
    }

    public void draw(boolean doTranslate) {
        GL11.glPushMatrix();
        if (doTranslate) GL11.glTranslatef(0, 0, 999);
        GL11.glScalef(scale, scale, 1.0F);
        drawOutLine(this.textRenderer, scaledPosition.getX(), scaledPosition.getY(), text, color, decoration);
        GL11.glPopMatrix();
    }

    private static void drawOutLine(TextRenderer textRenderer, int x, int y, String text, Integer color, TimerDecoration decoration) {
        if (decoration == TimerDecoration.OUTLINE) {
            textRenderer.draw(text, x + 1, y + 1, 0);
            textRenderer.draw(text, x + 1, y, 0);
            textRenderer.draw(text, x + 1, y - 1, 0);
            textRenderer.draw(text, x, y - 1, 0);
            textRenderer.draw(text, x, y + 1, 0);
            textRenderer.draw(text, x - 1, y + 1, 0);
            textRenderer.draw(text, x - 1, y, 0);
            textRenderer.draw(text, x - 1, y - 1, 0);
        } else if (decoration == TimerDecoration.SHADOW) {
            textRenderer.draw(text, x + 1, y + 1, -12566464);
        }
        textRenderer.draw(text, x, y, color);
    }


    private int getScaledTextWidth() {
        return Math.round(textWidth * scale);
    }

    private int getScaledTextHeight() {
        return Math.round(9 * scale);
    }
}