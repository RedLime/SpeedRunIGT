package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecimals;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.RenderableGlyph;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class TimerDrawer {

    public static final HashMap<String, Float> fontHeightMap = new HashMap<>();

    private final boolean translateZ;
    private final MinecraftClient client = MinecraftClient.getInstance();

    private float igtXPos;
    private float igtYPos;
    private float igtScale;
    private Integer igtColor;
    private TimerDecoration igtDecoration;

    private float rtaXPos;
    private float rtaYPos;
    private float rtaScale;
    private Integer rtaColor;
    private TimerDecoration rtaDecoration;

    private int igtPadding;
    private int rtaPadding;
    private float bgOpacity;

    private boolean simply;
    private boolean toggle;
    private boolean isLocked;
    private TimerDecimals timerDecimals;
    private Identifier timerFont;

    public TimerDrawer(boolean translateZ) {
        this(translateZ,
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_POSITION_X),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_POSITION_Y),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_SCALE),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_COLOR),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_IGT_DECO),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_POSITION_X),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_POSITION_Y),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_SCALE),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_COLOR),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_RTA_DECO),
                SpeedRunOptions.getOption(SpeedRunOptions.IGT_BACKGROUND_PADDING),
                SpeedRunOptions.getOption(SpeedRunOptions.RTA_BACKGROUND_PADDING),
                SpeedRunOptions.getOption(SpeedRunOptions.BACKGROUND_OPACITY),
                SpeedRunOptions.getOption(SpeedRunOptions.DISPLAY_TIME_ONLY),
                SpeedRunOptions.getOption(SpeedRunOptions.TOGGLE_TIMER),
                SpeedRunOptions.getOption(SpeedRunOptions.LOCK_TIMER_POSITION),
                SpeedRunOptions.getOption(SpeedRunOptions.DISPLAY_DECIMALS),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_TEXT_FONT));
    }

    public TimerDrawer(boolean translateZ,
                       float igtXPos, float igtYPos, float igtScale, Integer igtColor, TimerDecoration igtDecoration,
                       float rtaXPos, float rtaYPos, float rtaScale, Integer rtaColor, TimerDecoration rtaDecoration,
                       int igtPadding, int rtaPadding, float bgOpacity,
                       boolean simply, boolean toggle, boolean isLocked, TimerDecimals timerDecimals, Identifier timerFont) {
        this.translateZ = translateZ;
        this.igtXPos = igtXPos;
        this.igtYPos = igtYPos;
        this.igtScale = igtScale;
        this.igtColor = igtColor;
        this.igtDecoration = igtDecoration;
        this.rtaXPos = rtaXPos;
        this.rtaYPos = rtaYPos;
        this.rtaScale = rtaScale;
        this.rtaColor = rtaColor;
        this.rtaDecoration = rtaDecoration;
        this.igtPadding = igtPadding;
        this.rtaPadding = rtaPadding;
        this.bgOpacity = bgOpacity;
        this.simply = simply;
        this.toggle = toggle;
        this.isLocked = isLocked;
        this.timerDecimals = timerDecimals;
        this.timerFont = timerFont;
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

    public Integer getRTAColor() {
        return rtaColor;
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

    public void setRTAColor(Integer rtaColor) {
        this.rtaColor = rtaColor;
    }

    public TimerDecoration getIGTDecoration() {
        return igtDecoration;
    }

    public void setIGTDecoration(TimerDecoration igtDecoration) {
        this.igtDecoration = igtDecoration;
    }

    public TimerDecoration getRTADecoration() {
        return rtaDecoration;
    }

    public void setRTADecoration(TimerDecoration rtaDecoration) {
        this.rtaDecoration = rtaDecoration;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public TimerDecimals getTimerDecimals() {
        return timerDecimals;
    }

    public void setTimerDecimals(TimerDecimals timerDecimals) {
        this.timerDecimals = timerDecimals;
    }

    public Identifier getTimerFont() {
        return timerFont;
    }

    public void setTimerFont(Identifier timerFont) {
        this.timerFont = timerFont;
    }

    public int getIGTPadding() {
        return igtPadding;
    }

    public void setIGTPadding(int igtPadding) {
        this.igtPadding = igtPadding;
    }

    public int getRTAPadding() {
        return rtaPadding;
    }

    public void setRTAPadding(int rtaPadding) {
        this.rtaPadding = rtaPadding;
    }

    public float getBGOpacity() {
        return bgOpacity;
    }

    public void setBGOpacity(float bgOpacity) {
        this.bgOpacity = bgOpacity;
    }


    private String getTimeFormat(long time) {
        if (!InGameTimer.getInstance().isPlaying() && translateZ) {
            return InGameTimer.timeToStringFormat(time);
        }
        String millsString = String.format("%03d", time % 1000).substring(0, timerDecimals.getNumber());
        int seconds = ((int) (time / 1000)) % 60;
        int minutes = ((int) (time / 1000)) / 60;
        if (minutes > 59) {
            int hours = minutes / 60;
            minutes = minutes % 60;
            if (timerDecimals == TimerDecimals.NONE) {
                return String.format("%d:%02d:%02d", hours, minutes, seconds);
            }
            return String.format("%d:%02d:%02d.%s", hours, minutes, seconds, millsString);
        } else {
            if (timerDecimals == TimerDecimals.NONE) {
                return String.format("%02d:%02d", minutes, seconds);
            }
            return String.format("%02d:%02d.%s", minutes, seconds, millsString);
        }
    }

    public MutableText getIGTText() {
        return new LiteralText((this.simply ? "" : "IGT: ") + getTimeFormat(InGameTimer.getInstance().getInGameTime()));
    }

    public MutableText getRTAText() {
        return new LiteralText((this.simply ? "" : "RTA: ") + getTimeFormat(InGameTimer.getInstance().getRealTimeAttack()));
    }

    public void draw() {
        if (!toggle) return;

        client.getProfiler().push("create");
        MutableText igtText = getIGTText();
        MutableText rtaText = getRTAText();

        client.getProfiler().swap("font");
        //폰트 조정
        float fontHeight = 8;
        if (getTimerFont() != MinecraftClient.DEFAULT_FONT_ID && client.fontManager.fontStorages.containsKey(getTimerFont())) {
            rtaText.setStyle(rtaText.getStyle().withFont(getTimerFont()));
            igtText.setStyle(igtText.getStyle().withFont(getTimerFont()));
            fontHeight = fontHeightMap.computeIfAbsent(getTimerFont().toString(), key -> {
                RenderableGlyph glyph = client.fontManager.fontStorages.get(getTimerFont()).getRenderableGlyph('I');
                return glyph.getHeight() / glyph.getOversample();
            });
        }

        //초기 값 조정
        client.getProfiler().swap("init");
        TimerElement igtTimerElement = new TimerElement();
        TimerElement rtaTimerElement = new TimerElement();
        rtaTimerElement.init(rtaXPos, rtaYPos, rtaScale, rtaText, rtaColor, rtaDecoration, fontHeight);
        igtTimerElement.init(igtXPos, igtYPos, igtScale, igtText, igtColor, igtDecoration, fontHeight);

        MatrixStack matrixStack = new MatrixStack();

        //배경 렌더
        client.getProfiler().swap("background");
        if (bgOpacity > 0.01f) {
            Position rtaMin = new Position(rtaTimerElement.getPosition().getX() - rtaPadding, rtaTimerElement.getPosition().getY() - rtaPadding);
            Position rtaMax = new Position(rtaMin.getX() + rtaTimerElement.getScaledTextWidth() + ((rtaPadding - 1) + rtaPadding), rtaMin.getY() + rtaTimerElement.getScaledTextHeight() + ((rtaPadding - 1) + rtaPadding));
            Position igtMin = new Position(igtTimerElement.getPosition().getX() - igtPadding, igtTimerElement.getPosition().getY() - igtPadding);
            Position igtMax = new Position(igtMin.getX() + igtTimerElement.getScaledTextWidth() + ((igtPadding - 1) + igtPadding), igtMin.getY() + igtTimerElement.getScaledTextHeight() + ((igtPadding - 1) + igtPadding));
            int opacity = BackgroundHelper.ColorMixer.getArgb((int) (bgOpacity * 255), 0, 0, 0);
            if (rtaMin.getX() < igtMax.getX() && rtaMin.getY() < igtMax.getY() &&
                    igtMin.getX() < rtaMax.getX() && igtMin.getY() < rtaMax.getY()) {
                DrawableHelper.fill(matrixStack, Math.min(rtaMin.getX(), igtMin.getX()), Math.min(rtaMin.getY(), igtMin.getY()),
                        Math.max(rtaMax.getX(), igtMax.getX()), Math.max(rtaMax.getY(), igtMax.getY()), opacity);
            } else {
                if (rtaScale != 0) DrawableHelper.fill(matrixStack, rtaMin.getX(), rtaMin.getY(), rtaMax.getX(), rtaMax.getY(), opacity);
                if (igtScale != 0) DrawableHelper.fill(matrixStack, igtMin.getX(), igtMin.getY(), igtMax.getX(), igtMax.getY(), opacity);
            }
        }

        //렌더
        client.getProfiler().swap("draw");
        if (igtScale != 0) igtTimerElement.draw(matrixStack, translateZ);
        if (rtaScale != 0) rtaTimerElement.draw(matrixStack, translateZ);

        client.getProfiler().pop();
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
