package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class TimerDrawer {

    private final boolean translateZ;
    private final MinecraftClient client = MinecraftClient.getInstance();

    private float xPos;
    private float yPos;
    private float igtScale;
    private Position igtScaledPos;
    private float rtaScale;
    private Position rtaScaledPos;
    private float bgOpacity;
    private boolean reversed;
    private boolean simply;
    private boolean toggle;
    private Integer igtColor;
    private boolean igtDrawOutline;
    private Integer rtaColor;
    private boolean rtaDrawOutline;

    private boolean preUpdated = false;

    private int igtWidth;
    private int rtaWidth;
    private int scaledIGTWidth;
    private int scaledRTAWidth;
    private int igtWidthGap;
    private int rtaWidthGap;

    private int windowWidth;
    private int windowHeight;

    private int bgColor = 0;
    private int totalHeight;

    public TimerDrawer(boolean translateZ) {
        this(translateZ,
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POSITION_X),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POSITION_Y),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_SCALE),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_SCALE),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_BG_OPACITY),
                SpeedRunOptions.getOption(SpeedRunOptions.REVERSED_IGT_RTA),
                SpeedRunOptions.getOption(SpeedRunOptions.DISPLAY_TIME_ONLY),
                SpeedRunOptions.getOption(SpeedRunOptions.TOGGLE_TIMER),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_COLOR),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_OUTLINE),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_COLOR),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_OUTLINE));
    }

    public TimerDrawer(boolean translateZ, float xPos, float yPos, float igtScale, float rtaScale, float bgOpacity, boolean isReversed, boolean isSimply, boolean toggle, int igtColor, boolean igtDrawOutline, int rtaColor, boolean rtaDrawOutline) {
        this.translateZ = translateZ;
        this.xPos = xPos;
        this.yPos = yPos;
        this.igtScale = igtScale;
        this.rtaScale = rtaScale;
        this.bgOpacity = bgOpacity;
        this.reversed = isReversed;
        this.simply = isSimply;
        this.toggle = toggle;
        this.igtColor = igtColor;
        this.igtDrawOutline = igtDrawOutline;
        this.rtaColor = rtaColor;
        this.rtaDrawOutline = rtaDrawOutline;
    }

    public float getXPos() {
        return xPos;
    }

    public float getYPos() {
        return yPos;
    }

    public float getIGTScale() {
        return igtScale;
    }

    public float getRTAScale() {
        return rtaScale;
    }

    public float getBackgroundOpacity() {
        return bgOpacity;
    }

    public boolean isReversedOrder() {
        return reversed;
    }

    public boolean isSimplyTimer() {
        return simply;
    }

    public boolean isToggle() {
        return toggle;
    }

    public Integer getIGTColor() {
        return igtColor;
    }

    public boolean isIGTDrawOutline() {
        return igtDrawOutline;
    }

    public Integer getRTAColor() {
        return rtaColor;
    }

    public boolean isRTADrawOutline() {
        return rtaDrawOutline;
    }

    public void setIGTScale(float igtScale) {
        this.igtScale = igtScale;
        this.preUpdated = false;
    }

    public void setRTAScale(float rtaScale) {
        this.rtaScale = rtaScale;
        this.preUpdated = false;
    }

    public void setXPos(float xPos) {
        this.xPos = xPos;
        this.preUpdated = false;
    }

    public void setYPos(float yPos) {
        this.yPos = yPos;
        this.preUpdated = false;
    }

    public void setBackgroundOpacity(float bgOpacity) {
        this.bgOpacity = bgOpacity;
        this.preUpdated = false;
    }

    public void setReversedOrder(boolean reversed) {
        this.reversed = reversed;
        this.preUpdated = false;
    }

    public void setSimplyTimer(boolean simply) {
        this.simply = simply;
        this.preUpdated = false;
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
        this.preUpdated = false;
    }

    public void setIGTColor(Integer igtColor) {
        this.igtColor = igtColor;
        this.preUpdated = false;
    }

    public void setIGTDrawOutline(boolean igtDrawOutline) {
        this.igtDrawOutline = igtDrawOutline;
        this.preUpdated = false;
    }

    public void setRTAColor(Integer rtaColor) {
        this.rtaColor = rtaColor;
        this.preUpdated = false;
    }

    public void setRTADrawOutline(boolean rtaDrawOutline) {
        this.rtaDrawOutline = rtaDrawOutline;
        this.preUpdated = false;
    }

    private void updatePos() {
        if (client.textRenderer == null) return;
        TextRenderer textRenderer = client.textRenderer;

        this.windowWidth = this.client.getWindow().getScaledWidth();
        this.windowHeight = this.client.getWindow().getScaledHeight();
        int translateX = (int) (this.xPos * this.windowWidth);
        int translateY = (int) (this.yPos * this.windowHeight);

        this.igtScaledPos = new Position((int) (translateX / this.igtScale), (int) (translateY / this.igtScale));
        this.rtaScaledPos = new Position((int) (translateX / this.rtaScale), (int) (translateY / this.rtaScale));

        this.igtWidth = textRenderer.getWidth(getIGTText());
        this.rtaWidth = textRenderer.getWidth(getRTAText());
        this.scaledIGTWidth = (int) (this.igtWidth * this.igtScale);
        this.scaledRTAWidth = (int) (this.rtaWidth * this.rtaScale);

        int maxWidth = Math.max(scaledIGTWidth, scaledRTAWidth);

        if (maxWidth + translateX > this.windowWidth) {
            this.igtScaledPos.setX(igtScaledPos.getX() - ((int) (Math.max(scaledIGTWidth, scaledRTAWidth) / this.igtScale)) + 2);
            this.rtaScaledPos.setX(rtaScaledPos.getX() - ((int) (Math.max(scaledIGTWidth, scaledRTAWidth) / this.rtaScale)) + 2);
        }

        this.totalHeight = ((int) (9 * this.igtScale)) + ((int) (9 * this.rtaScale)) + 1;
        if (translateY + totalHeight > this.windowHeight) {
            this.igtScaledPos.setY(igtScaledPos.getY() - MathHelper.floor(totalHeight / igtScale) + 2);
            this.rtaScaledPos.setY(rtaScaledPos.getY() - MathHelper.floor(totalHeight / rtaScale) + 2);
        }

        int gap = this.scaledIGTWidth - this.scaledRTAWidth;
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
        if (!toggle) return;

        TextRenderer textRenderer = client.textRenderer;
        MutableText igt = getIGTText();
        MutableText rta = getRTAText();

        if (!preUpdated || windowWidth != client.getWindow().getScaledWidth() || windowHeight != client.getWindow().getScaledHeight()
            || Math.max(igtWidth, rtaWidth) != Math.max(textRenderer.getWidth(igt), textRenderer.getWidth(igt)))
            updatePos();
        if (!preUpdated) return;

        client.getProfiler().push("timer");

        MatrixStack matrixStack = new MatrixStack();

        matrixStack.push();
        if (this.translateZ) matrixStack.translate(0, 0, 1000);
        matrixStack.scale(1f, 1f, 1f);
        float maxScale = Math.max(igtScale, rtaScale);
        Position timerPos = new Position((int) (this.reversed ? (igtScaledPos.getX() * igtScale) : (rtaScaledPos.getX() * rtaScale)), (int) (this.reversed ? (igtScaledPos.getY() * igtScale) : (rtaScaledPos.getY() * rtaScale)));
        float bgWidth = 3 * maxScale;
        DrawableHelper.fill(matrixStack, (int) (timerPos.getX() - (3 * maxScale) - maxScale), timerPos.getY() - MathHelper.ceil(5 * (!this.reversed ? igtScale : rtaScale)),
                (int) (timerPos.getX() + Math.max(scaledIGTWidth, scaledRTAWidth) + bgWidth), timerPos.getY() + totalHeight + MathHelper.floor(3.5 * (this.reversed ? igtScale : rtaScale)), bgColor);
        matrixStack.pop();

        matrixStack.push();
        if (this.translateZ) matrixStack.translate(0, 0, 1000);
        matrixStack.scale(igtScale, igtScale, 1f);
        Position igtPos = new Position(igtScaledPos.getX() + (int) (igtWidthGap / igtScale),
                igtScaledPos.getY() + (this.reversed ? (int) Math.ceil(8 * this.rtaScale / igtScale) + 2 : 0));
        drawOutLine(textRenderer, matrixStack, igtPos.getX(), igtPos.getY(), igt, igtColor, igtDrawOutline);
        matrixStack.pop();

        matrixStack.push();
        if (this.translateZ) matrixStack.translate(0, 0, 1000);
        matrixStack.scale(rtaScale, rtaScale, 1f);
        Position rtaPos = new Position(rtaScaledPos.getX() + (int) (rtaWidthGap / rtaScale),
                rtaScaledPos.getY() + (!this.reversed ? (int) Math.ceil(8 * this.igtScale / rtaScale) + 2 : 0));
        drawOutLine(textRenderer, matrixStack, rtaPos.getX(), rtaPos.getY(), rta, rtaColor, rtaDrawOutline);
        //drawOutLine(textRenderer, matrixStack, 20, 20, new LiteralText(SpeedRunIGT.DEBUG_DATA), Formatting.RED.getColorValue(), true);
        matrixStack.pop();


        client.getProfiler().pop();
    }

    private void drawOutLine(TextRenderer textRenderer, MatrixStack matrixStack, int x, int y, MutableText text, Integer color, boolean drawOutline) {
        if (drawOutline) {
            textRenderer.draw(matrixStack, text, (float)x + 1, (float)y + 1, 0);
            textRenderer.draw(matrixStack, text, (float)x + 1, (float)y, 0);
            textRenderer.draw(matrixStack, text, (float)x + 1, (float)y - 1, 0);
            textRenderer.draw(matrixStack, text, (float)x, (float)y - 1, 0);
            textRenderer.draw(matrixStack, text, (float)x, (float)y + 1, 0);
            textRenderer.draw(matrixStack, text, (float)x - 1, (float)y + 1, 0);
            textRenderer.draw(matrixStack, text, (float)x - 1, (float)y, 0);
            textRenderer.draw(matrixStack, text, (float)x - 1, (float)y - 1, 0);
        }
        textRenderer.draw(matrixStack, text, (float)x, (float)y, color);
    }


    public static class Position {
        private int x;
        private int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

    }
}
