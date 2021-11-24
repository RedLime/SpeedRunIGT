package com.redlimerl.speedrunigt.timer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Matrix4f;

public class TimerDrawer {

    private final InGameTimer timer;
    private final boolean translateZ;
    private final MinecraftClient client = MinecraftClient.getInstance();

    private static final int height = 18;

    private float xPos = 0.035f;
    private float yPos = 0.035f;
    private float scaleX = 12;
    private float scaleY = 12;
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

    public TimerDrawer(InGameTimer timer) {
        this(timer, true);
    }

    public TimerDrawer(InGameTimer timer, boolean translateZ) {
        this.timer = timer;
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
        float translateX = this.xPos * this.windowWidth;
        float translateY = this.yPos * this.windowHeight;
        this.scaleX = translateX / this.scale;
        this.scaleY = translateY / this.scale;

        this.igtWidth = textRenderer.getWidth(getIGTText());
        this.rtaWidth = textRenderer.getWidth(getRTAText());
        float scaledIGTWidth = this.igtWidth * this.scale;
        float scaledRTAWidth = this.rtaWidth * this.scale;

        int gap = this.igtWidth - this.rtaWidth;
        float maxWidth = Math.max(scaledIGTWidth, scaledRTAWidth);

        if (maxWidth + translateX > this.windowWidth) {
            this.scaleX = this.scaleX - ((int) (Math.max(scaledIGTWidth, scaledRTAWidth) / this.scale)) + 2;
        }

        int rowSize = (int) (height * this.scale);
        if (translateY + rowSize > this.windowHeight) {
            this.scaleY = this.scaleY - height + 2;
        }

        boolean rightSide = (translateX + (maxWidth / 2f)) > this.windowWidth / 2f;
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

    public MutableText getIGTText() { return new LiteralText(this.simply ? "" : "IGT: ").append(new LiteralText(InGameTimer.timeToStringFormat(this.timer.getInGameTime()))); }

    public MutableText getRTAText() { return new LiteralText(this.simply ? "" : "RTA: ").append(new LiteralText(InGameTimer.timeToStringFormat(this.timer.getRealTimeAttack()))); }

    public void draw() {
        if (!preUpdated || windowWidth != client.getWindow().getScaledWidth() || windowHeight != client.getWindow().getScaledHeight()) updatePos();
        if (!preUpdated) return;

        TextRenderer textRenderer = client.textRenderer;

        MatrixStack matrixStack = new MatrixStack();
        MutableText igt = getIGTText();
        MutableText rta = getRTAText();

        matrixStack.push();
        if (this.translateZ) matrixStack.translate(0, 0, 1000);
        matrixStack.scale(scale, scale, 1f);
        int bgWidth = 3;
        //DrawableHelper.fill - int to float
        fill(matrixStack.peek().getModel(), scaleX - bgWidth - 1, scaleY - bgWidth - 1,
                scaleX + Math.max(igtWidth, rtaWidth) + bgWidth, scaleY + height + bgWidth, bgColor);
        drawOutLine(textRenderer, matrixStack, scaleX + igtWidthGap, scaleY + (this.reversed ? 10 : 0), igt, Formatting.YELLOW.getColorValue());
        drawOutLine(textRenderer, matrixStack, scaleX + rtaWidthGap, scaleY + (this.reversed ? 0 : 10), rta, Formatting.AQUA.getColorValue());
        //drawOutLine(textRenderer, matrixStack, scaleX + rtaWidthGap, scaleY + 20, new LiteralText(timer.getStatus().name()), Formatting.RED);
        matrixStack.pop();
    }

    private void drawOutLine(TextRenderer textRenderer, MatrixStack matrixStack, float x, float y, MutableText text, Integer color) {
        textRenderer.draw(matrixStack, text, x + 1, y + 1, 0);
        textRenderer.draw(matrixStack, text, x + 1, y, 0);
        textRenderer.draw(matrixStack, text, x + 1, y - 1, 0);
        textRenderer.draw(matrixStack, text, x, y - 1, 0);
        textRenderer.draw(matrixStack, text, x, y + 1, 0);
        textRenderer.draw(matrixStack, text, x - 1, y + 1, 0);
        textRenderer.draw(matrixStack, text, x - 1, y, 0);
        textRenderer.draw(matrixStack, text, x - 1, y - 1, 0);
        textRenderer.draw(matrixStack, text, x, y, color);
    }

    private static void fill(Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        float j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }

        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x2, y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
