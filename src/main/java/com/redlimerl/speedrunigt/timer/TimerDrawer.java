package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

public class TimerDrawer {

    private final boolean translateZ;
    private final MinecraftClient client = MinecraftClient.getInstance();

    private float igtXPos;
    private float igtYPos;
    private float igtScale;
    private Integer igtColor;
    private boolean igtDrawOutline;

    private float rtaXPos;
    private float rtaYPos;
    private float rtaScale;
    private Integer rtaColor;
    private boolean rtaDrawOutline;

    private boolean simply;
    private boolean toggle;

    public TimerDrawer(boolean translateZ) {
        this(translateZ,
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_POSITION_X),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_POSITION_Y),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_SCALE),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_COLOR),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_OUTLINE),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_POSITION_X),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_POSITION_Y),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_SCALE),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_COLOR),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_OUTLINE),
                SpeedRunOptions.getOption(SpeedRunOptions.DISPLAY_TIME_ONLY),
                SpeedRunOptions.getOption(SpeedRunOptions.TOGGLE_TIMER));
    }

    public TimerDrawer(boolean translateZ,
                       float igtXPos, float igtYPos, float igtScale, Integer igtColor, boolean igtDrawOutline,
                       float rtaXPos, float rtaYPos, float rtaScale, Integer rtaColor, boolean rtaDrawOutline,
                       boolean simply, boolean toggle) {
        this.translateZ = translateZ;
        this.igtXPos = igtXPos;
        this.igtYPos = igtYPos;
        this.igtScale = igtScale;
        this.igtColor = igtColor;
        this.igtDrawOutline = igtDrawOutline;
        this.rtaXPos = rtaXPos;
        this.rtaYPos = rtaYPos;
        this.rtaScale = rtaScale;
        this.rtaColor = rtaColor;
        this.rtaDrawOutline = rtaDrawOutline;
        this.simply = simply;
        this.toggle = toggle;
    }

    public float getIGT_XPos() {
        return igtXPos;
    }

    public void setIGT_XPos(float igtXPos) {
        this.igtXPos = igtXPos;
    }

    public float getIGT_YPos() {
        return igtYPos;
    }

    public void setIGT_YPos(float igtYPos) {
        this.igtYPos = igtYPos;
    }

    public float getRTA_XPos() {
        return rtaXPos;
    }

    public void setRTA_XPos(float rtaXPos) {
        this.rtaXPos = rtaXPos;
    }

    public float getRTA_YPos() {
        return rtaYPos;
    }

    public void setRTA_YPos(float rtaYPos) {
        this.rtaYPos = rtaYPos;
    }

    public float getIGTScale() {
        return igtScale;
    }

    public float getRTAScale() {
        return rtaScale;
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
    }

    public void setRTAScale(float rtaScale) {
        this.rtaScale = rtaScale;
    }

    public void setSimplyTimer(boolean simply) {
        this.simply = simply;
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
    }

    public void setIGTColor(Integer igtColor) {
        this.igtColor = igtColor;
    }

    public void setIGTDrawOutline(boolean igtDrawOutline) {
        this.igtDrawOutline = igtDrawOutline;
    }

    public void setRTAColor(Integer rtaColor) {
        this.rtaColor = rtaColor;
    }

    public void setRTADrawOutline(boolean rtaDrawOutline) {
        this.rtaDrawOutline = rtaDrawOutline;
    }

    public MutableText getIGTText() {
        return new LiteralText(this.simply ? "" : "IGT: ").append(new LiteralText(InGameTimer.timeToStringFormat(InGameTimer.getInstance().getInGameTime())));
    }

    public MutableText getRTAText() {
        return new LiteralText(this.simply ? "" : "RTA: ").append(new LiteralText(InGameTimer.timeToStringFormat(InGameTimer.getInstance().getRealTimeAttack())));
    }

    public void draw() {
        if (!toggle) return;

        client.getProfiler().push("timer");

        MutableText igtText = getIGTText();
        MutableText rtaText = getRTAText();

        TimerElement igtTimerElement = new TimerElement();
        TimerElement rtaTimerElement = new TimerElement();

        //초기 값 조정
        rtaTimerElement.init(rtaXPos, rtaYPos, rtaScale, rtaText, rtaColor, rtaDrawOutline);
        igtTimerElement.init(igtXPos, igtYPos, igtScale, igtText, igtColor, igtDrawOutline);

        //렌더
        MatrixStack matrixStack = new MatrixStack();
        if (igtScale != 0) igtTimerElement.draw(matrixStack, translateZ);
        if (rtaScale != 0) rtaTimerElement.draw(matrixStack, translateZ);

        client.getProfiler().pop();
    }

    static void drawOutLine(TextRenderer textRenderer, MatrixStack matrixStack, int x, int y, MutableText text, Integer color, boolean drawOutline) {
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
