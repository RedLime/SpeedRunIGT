package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecimals;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class TimerDrawer {

    public static Identifier DEFAULT_FONT = new Identifier("default");

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
                SpeedRunOptions.getOption(SpeedRunOptions.DISPLAY_TIME_ONLY),
                SpeedRunOptions.getOption(SpeedRunOptions.TOGGLE_TIMER),
                SpeedRunOptions.getOption(SpeedRunOptions.LOCK_TIMER_POSITION),
                SpeedRunOptions.getOption(SpeedRunOptions.DISPLAY_DECIMALS),
                SpeedRunOptions.getOption(SpeedRunOptions.TIMER_TEXT_FONT));
    }

    public TimerDrawer(boolean translateZ,
                       float igtXPos, float igtYPos, float igtScale, Integer igtColor, TimerDecoration igtDecoration,
                       float rtaXPos, float rtaYPos, float rtaScale, Integer rtaColor, TimerDecoration rtaDecoration,
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

    public String getIGTText() {
        return (this.simply ? "" : "IGT: ") + getTimeFormat(InGameTimer.getInstance().getInGameTime());
    }

    public String getRTAText() {
        return (this.simply ? "" : "RTA: ") + getTimeFormat(InGameTimer.getInstance().getRealTimeAttack());
    }

    public void draw() {
        if (!toggle) return;

        client.getProfiler().push("timer");

        String igtText = getIGTText();
        String rtaText = getRTAText();

        client.getProfiler().swap("font");
        //폰트 조정
        TextRenderer targetFont = client.textRenderer;
        if (getTimerFont() != DEFAULT_FONT && client.fontManager.textRenderers.containsKey(getTimerFont())) {
            targetFont = client.fontManager.textRenderers.get(getTimerFont());
        }

        //초기 값 조정
        client.getProfiler().swap("init");

        TimerElement igtTimerElement = new TimerElement(targetFont);
        TimerElement rtaTimerElement = new TimerElement(targetFont);
        rtaTimerElement.init(rtaXPos, rtaYPos, rtaScale, rtaText, rtaColor, rtaDecoration);
        igtTimerElement.init(igtXPos, igtYPos, igtScale, igtText, igtColor, igtDecoration);


        //렌더
        client.getProfiler().swap("draw");
        if (igtScale != 0) igtTimerElement.draw(translateZ);
        if (rtaScale != 0) rtaTimerElement.draw(translateZ);

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
