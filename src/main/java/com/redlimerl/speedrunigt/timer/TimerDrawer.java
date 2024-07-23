package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.mixins.access.FontManagerAccessor;
import com.redlimerl.speedrunigt.mixins.access.MinecraftClientAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecimals;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDisplayAlign;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class TimerDrawer {

    public enum PositionType {
        DEFAULT, WHILE_F3, WHILE_PAUSED
    }

    public static final HashMap<String, Float> fontHeightMap = new HashMap<>();

    private final boolean translateZ;
    private final MinecraftClient client = MinecraftClient.getInstance();

    private boolean needUpdate = false;

    private float igtXPos;
    private float igtYPos;
    private float igtScale;
    private Integer igtColor;
    private TimerDecoration igtDecoration;
    private TimerDisplayAlign igtDisplayAlign;

    private float rtaXPos;
    private float rtaYPos;
    private float rtaScale;
    private Integer rtaColor;
    private TimerDecoration rtaDecoration;
    private TimerDisplayAlign rtaDisplayAlign;

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
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_X),
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_Y),
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_SCALE),
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_COLOR),
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_DECO),
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_DISPLAY_ALIGN),
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_X),
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_Y),
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_SCALE),
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_COLOR),
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_DECO),
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_DISPLAY_ALIGN),
                SpeedRunOption.getOption(SpeedRunOptions.IGT_BACKGROUND_PADDING),
                SpeedRunOption.getOption(SpeedRunOptions.RTA_BACKGROUND_PADDING),
                SpeedRunOption.getOption(SpeedRunOptions.BACKGROUND_OPACITY),
                SpeedRunOption.getOption(SpeedRunOptions.DISPLAY_TIME_ONLY),
                SpeedRunOption.getOption(SpeedRunOptions.TOGGLE_TIMER),
                SpeedRunOption.getOption(SpeedRunOptions.LOCK_TIMER_POSITION),
                SpeedRunOption.getOption(SpeedRunOptions.DISPLAY_DECIMALS),
                SpeedRunOption.getOption(SpeedRunOptions.TIMER_TEXT_FONT));
    }

    public TimerDrawer(boolean translateZ,
                       float igtXPos, float igtYPos, float igtScale, Integer igtColor, TimerDecoration igtDecoration, TimerDisplayAlign igtDisplayAlign,
                       float rtaXPos, float rtaYPos, float rtaScale, Integer rtaColor, TimerDecoration rtaDecoration, TimerDisplayAlign rtaDisplayAlign,
                       int igtPadding, int rtaPadding, float bgOpacity,
                       boolean simply, boolean toggle, boolean isLocked, TimerDecimals timerDecimals, Identifier timerFont) {
        this.translateZ = translateZ;
        this.igtXPos = igtXPos;
        this.igtYPos = igtYPos;
        this.igtScale = igtScale;
        this.igtColor = igtColor;
        this.igtDecoration = igtDecoration;
        this.igtDisplayAlign = igtDisplayAlign;
        this.rtaXPos = rtaXPos;
        this.rtaYPos = rtaYPos;
        this.rtaScale = rtaScale;
        this.rtaColor = rtaColor;
        this.rtaDecoration = rtaDecoration;
        this.rtaDisplayAlign = rtaDisplayAlign;
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

    public TimerDisplayAlign getRTADisplayAlign() {
        return rtaDisplayAlign;
    }

    public void setRTADisplayAlign(TimerDisplayAlign rtaDisplayAlign) {
        this.rtaDisplayAlign = rtaDisplayAlign;
    }

    public TimerDisplayAlign getIGTDisplayAlign() {
        return igtDisplayAlign;
    }

    public void setIGTDisplayAlign(TimerDisplayAlign igtDisplayAlign) {
        this.igtDisplayAlign = igtDisplayAlign;
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

    public void update() {
        this.needUpdate = true;
    }

    public boolean isNeedUpdate() {
        boolean result = this.needUpdate;
        this.needUpdate = false;
        return result;
    }

    private String getTimeFormat(long time) {
        if ((InGameTimer.getInstance().isCompleted() || InGameTimer.getInstance().isPaused()) && translateZ) {
            return InGameTimerUtils.timeToStringFormat(time);
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
        InGameTimer timer = InGameTimer.getInstance();

        if (SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE) && timer.isServerIntegrated && InGameTimerUtils.getServer() != null && SpeedRunIGT.IS_CLIENT_SIDE) {
            Long inGameTime = timer.isCompleted() ? timer.getCompleteStatIGT() : InGameTimerClientUtils.getPlayerTime();
            if (inGameTime != null) return Text.literal((this.simply ? "" : "IGT: ") + getTimeFormat(inGameTime));
        }

        long igt = timer.isCompleted() && SpeedRunOption.getOption(SpeedRunOptions.AUTO_RETIME_FOR_GUIDELINE)
                && timer.getCategory() == RunCategories.ANY && timer.getRunType() == RunType.RANDOM_SEED
                && (System.currentTimeMillis() / 3000) % 2 == 0
                ? timer.getRetimedInGameTime() : timer.getInGameTime();
        return Text.literal((this.simply ? "" : "IGT: ") + getTimeFormat(igt));
    }

    public MutableText getRTAText() {
        return Text.literal((this.simply ? "" : "RTA: ") + getTimeFormat(InGameTimer.getInstance().getRealTimeAttack()));
    }

    public void draw() {
        if (!toggle) return;

        MutableText igtText = getIGTText();
        MutableText rtaText = getRTAText();

        //폰트 조정
        float fontHeight = 8;
        if (!SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE)) {

            FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftClientAccessor) client).getFontManager();
            if (getTimerFont() != MinecraftClient.DEFAULT_FONT_ID && fontManager.getFontStorages().containsKey(getTimerFont())) {
                rtaText.setStyle(rtaText.getStyle().withFont(getTimerFont()));
                igtText.setStyle(igtText.getStyle().withFont(getTimerFont()));
                if (!fontHeightMap.containsKey(getTimerFont().toString())) {
                    fontManager.getFontStorages().get(getTimerFont()).getGlyph('I', false).bake(glyph -> {
                        fontHeightMap.put(getTimerFont().toString(), glyph.getHeight() / glyph.getOversample());
                        return null;
                    });
                }
                fontHeight = fontHeightMap.get(getTimerFont().toString());
            }

        }

        //초기 값 조정
        TimerElement igtTimerElement = new TimerElement();
        TimerElement rtaTimerElement = new TimerElement();
        rtaTimerElement.init(rtaXPos, rtaYPos, rtaScale, rtaText, rtaColor, rtaDecoration, rtaDisplayAlign, fontHeight);
        igtTimerElement.init(igtXPos, igtYPos, igtScale, igtText, igtColor, igtDecoration, igtDisplayAlign, fontHeight);

        MatrixStack matrixStack = new MatrixStack();

        //배경 렌더
        matrixStack.push();
        if (translateZ) matrixStack.translate(0, 0, 998);
        if (bgOpacity > 0.01f) {
            Position rtaMin = new Position(rtaTimerElement.getPosition().getX() - rtaPadding, rtaTimerElement.getPosition().getY() - rtaPadding);
            Position rtaMax = new Position(rtaMin.getX() + rtaTimerElement.getScaledTextWidth() + ((rtaPadding - 1) + rtaPadding), rtaMin.getY() + rtaTimerElement.getScaledTextHeight() + ((rtaPadding - 1) + rtaPadding));
            Position igtMin = new Position(igtTimerElement.getPosition().getX() - igtPadding, igtTimerElement.getPosition().getY() - igtPadding);
            Position igtMax = new Position(igtMin.getX() + igtTimerElement.getScaledTextWidth() + ((igtPadding - 1) + igtPadding), igtMin.getY() + igtTimerElement.getScaledTextHeight() + ((igtPadding - 1) + igtPadding));
            int opacity = ColorHelper.Argb.getArgb((int) (bgOpacity * 255), 0, 0, 0);
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
        if (igtScale != 0) igtTimerElement.draw(matrixStack, translateZ);
        if (rtaScale != 0) rtaTimerElement.draw(matrixStack, translateZ);
        matrixStack.pop();

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
