package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class TimerDrawer {

    private final boolean translateZ;
    private final MinecraftClient client = MinecraftClient.getInstance();

    private static final int height = 18;

    private float xPos = 0.035f;
    private float yPos = 0.035f;
    private int scaleX = 12;
    private int scaleY = 12;
    private float scale = 1.0f;
    private float bgOpacity = 1.0f;
    private boolean reversed = false;
    private boolean simply = false;

    private boolean preUpdated = false;

    private int igtWidth;
    private int rtaWidth;
    private int igtWidthGap;
    private int rtaWidthGap;

    private int windowWidth;
    private int windowHeight;

    private int bgColor = 0;

    public TimerDrawer(boolean translateZ) {
        this.translateZ = translateZ;
        this.setStatus(SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POSITION_X),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POSITION_Y),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_SCALE),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_BG_OPACITY),
                SpeedRunOptions.getOption(SpeedRunOptions.REVERSED_IGT_RTA),
                SpeedRunOptions.getOption(SpeedRunOptions.DISPLAY_TIME_ONLY));
    }

    public float getXPos() {
        return xPos;
    }

    public float getYPos() {
        return yPos;
    }

    public float getScale() {
        return scale;
    }

    public float getBgOpacity() {
        return bgOpacity;
    }

    public boolean isReversed() {
        return reversed;
    }

    public boolean isSimply() {
        return simply;
    }

    public void setStatus(float xPos, float yPos, float scale, float bgOpacity, boolean isReversed, boolean isSimply) {
        this.scale = scale;
        this.xPos = xPos;
        this.yPos = yPos;
        this.bgOpacity = bgOpacity;
        this.reversed = isReversed;
        this.simply = isSimply;
        this.preUpdated = false;
    }

    private void updatePos() {
        if (client.textRenderer == null) return;
        TextRenderer textRenderer = client.textRenderer;

        this.windowWidth = this.client.getWindow().getScaledWidth();
        this.windowHeight = this.client.getWindow().getScaledHeight();
        int translateX = (int) (this.xPos * this.windowWidth);
        int translateY = (int) (this.yPos * this.windowHeight);
        this.scaleX = (int) (translateX / this.scale);
        this.scaleY = (int) (translateY / this.scale);

        this.igtWidth = textRenderer.getWidth(getIGTText());
        this.rtaWidth = textRenderer.getWidth(getRTAText());
        int scaledIGTWidth = (int) (this.igtWidth * this.scale);
        int scaledRTAWidth = (int) (this.rtaWidth * this.scale);

        int gap = this.igtWidth - this.rtaWidth;
        int maxWidth = Math.max(scaledIGTWidth, scaledRTAWidth);

        if (maxWidth + translateX > this.windowWidth) {
            this.scaleX = this.scaleX - ((int) (Math.max(scaledIGTWidth, scaledRTAWidth) / this.scale)) + 2;
        }

        int rowSize = (int) (height * this.scale);
        if (translateY + rowSize > this.windowHeight) {
            this.scaleY = this.scaleY - height + 2;
        }

        boolean rightSide = (translateX + (maxWidth / 2)) > this.windowWidth / 2;
        if (!rightSide) {
            this.igtWidthGap = 0;
            this.rtaWidthGap = 0;
        } else if (gap >= 0) {
            this.igtWidthGap = 0;
            this.rtaWidthGap = gap;
        } else {
            this.rtaWidthGap = 0;
            this.igtWidthGap = -gap;
        }

        this.bgColor = BackgroundHelper.ColorMixer.getArgb((int) (bgOpacity * 255), 0, 0, 0);
        this.preUpdated = true;
    }

    public MutableText getIGTText() {
        return new LiteralText(this.simply ? "" : "IGT: ").append(new LiteralText(InGameTimer.timeToStringFormat(InGameTimer.getInstance().getInGameTime())));
    }

    public MutableText getRTAText() {
        return new LiteralText(this.simply ? "" : "RTA: ").append(new LiteralText(InGameTimer.timeToStringFormat(InGameTimer.getInstance().getRealTimeAttack())));
    }

    public void draw() {
        if (!preUpdated || windowWidth != client.getWindow().getScaledWidth() || windowHeight != client.getWindow().getScaledHeight())
            updatePos();
        if (!preUpdated) return;

        TextRenderer textRenderer = client.textRenderer;

        MatrixStack matrixStack = new MatrixStack();
        MutableText igt = getIGTText();
        MutableText rta = getRTAText();

        matrixStack.push();
        if (this.translateZ) matrixStack.translate(0, 0, 1000);
        matrixStack.scale(scale, scale, 1f);
        int bgWidth = 3;
        DrawableHelper.fill(matrixStack, scaleX - bgWidth - 1, scaleY - bgWidth - 1,
                scaleX + Math.max(igtWidth, rtaWidth) + bgWidth, scaleY + height + bgWidth, bgColor);
        drawOutLine(textRenderer, matrixStack, scaleX + igtWidthGap, scaleY + (this.reversed ? 10 : 0), igt, Formatting.YELLOW.getColorValue());
        drawOutLine(textRenderer, matrixStack, scaleX + rtaWidthGap, scaleY + (this.reversed ? 0 : 10), rta, Formatting.AQUA.getColorValue());
        //drawOutLine(textRenderer, matrixStack, scaleX + rtaWidthGap, scaleY + 20, new LiteralText(SpeedRunIGT.DEBUG_DATA), Formatting.RED.getColorValue());
        matrixStack.pop();
    }

    private void drawOutLine(TextRenderer textRenderer, MatrixStack matrixStack, int x, int y, MutableText text, Integer color) {
        textRenderer.draw(matrixStack, text, (float)x + 1, (float)y + 1, 0);
        textRenderer.draw(matrixStack, text, (float)x + 1, (float)y, 0);
        textRenderer.draw(matrixStack, text, (float)x + 1, (float)y - 1, 0);
        textRenderer.draw(matrixStack, text, (float)x, (float)y - 1, 0);
        textRenderer.draw(matrixStack, text, (float)x, (float)y + 1, 0);
        textRenderer.draw(matrixStack, text, (float)x - 1, (float)y + 1, 0);
        textRenderer.draw(matrixStack, text, (float)x - 1, (float)y, 0);
        textRenderer.draw(matrixStack, text, (float)x - 1, (float)y - 1, 0);
        textRenderer.draw(matrixStack, text, (float)x, (float)y, color);
    }
}
