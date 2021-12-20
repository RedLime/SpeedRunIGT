package com.redlimerl.speedrunigt.timer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.redlimerl.speedrunigt.timer.TimerDrawer.Position;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class TimerElement {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private final Position position = new Position(0, 0);
    private final Position scaledPosition = new Position(0, 0);
    private float scale = 1;
    private int textWidth = 0;
    private String text;
    private Integer color;
    private boolean doDrawOutline;

    public void init(float xPos, float yPos, float scale, String text, Integer color, boolean doDrawOutline) {
        this.scale = scale;
        this.text = text;
        this.color = color;
        this.doDrawOutline = doDrawOutline;
        int scaledWindowWidth = client.getWindow().getScaledWidth();
        int scaledWindowHeight = client.getWindow().getScaledHeight();

        int translateX = (int) (xPos * scaledWindowWidth);
        int translateY = (int) (yPos * scaledWindowHeight);

        this.position.setX(translateX);
        this.position.setY(translateY);
        this.scaledPosition.setX(Math.round(translateX / this.scale));
        this.scaledPosition.setY(Math.round(translateY / this.scale));

        this.textWidth = client.textRenderer.getStringWidth(text);

        //가로 화면 밖으로 나갈 시 재조정
        if (getScaledTextWidth() + this.position.getX() > scaledWindowWidth) {
            this.scaledPosition.setX(this.scaledPosition.getX() - Math.round((getScaledTextWidth() - 1) / scale));
        }
        //세로 화면 밖으로 나갈 시 재조정
        if (getScaledTextHeight() + this.position.getY() > scaledWindowHeight) {
            this.scaledPosition.setY(this.scaledPosition.getY() - MathHelper.floor((getScaledTextHeight() - 1) / scale));
        }
    }

    public void draw(MatrixStack matrixStack, boolean doTranslate) {
        RenderSystem.pushMatrix();
        if (doTranslate) RenderSystem.translatef(0, 0, 999);
        RenderSystem.scalef(scale, scale, 1.0F);
        TimerDrawer.drawOutLine(client.textRenderer, matrixStack, scaledPosition.getX(), scaledPosition.getY(), text, color, doDrawOutline);
        RenderSystem.popMatrix();
    }

    private int getScaledTextWidth() {
        return Math.round(textWidth * scale);
    }

    private int getScaledTextHeight() {
        return Math.round(9 * scale);
    }
}
