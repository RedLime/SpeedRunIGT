package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.gui.screen.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.mixins.access.GameMenuScreenAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final
    MinecraftClient client;
    private TimerDrawer.PositionType currentPositionType = TimerDrawer.PositionType.DEFAULT;
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/gui/DrawContext;)V", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void drawTimer(float tickDelta, long startTime, boolean tick, CallbackInfo ci, float f, boolean bl, int i, int j, Window window, Matrix4f matrix4f, MatrixStack matrixStack, DrawContext drawContext) {
        InGameTimer timer = InGameTimer.getInstance();

        if (InGameTimerClientUtils.canUnpauseTimer(true)) {
            if (!(InGameTimerUtils.isWaitingFirstInput() && !timer.isStarted())) {
                timer.setPause(false, "rendered");
            } else {
                timer.updateFirstRendered();
            }
        }

        long time = System.currentTimeMillis() - InGameTimerUtils.LATEST_TIMER_TIME;
        if (time < 2950) {
            String text = "SpeedRunIGT v" + (SpeedRunIGT.MOD_VERSION.split("\\+")[0]);
            drawContext.drawText(this.client.textRenderer, text, this.client.currentScreen != null ? (int) ((this.client.getWindow().getScaledWidth() - this.client.textRenderer.getWidth(text)) / 2f) : 4, this.client.getWindow().getScaledHeight() - 12,
                    ColorHelper.Argb.getArgb((int) (MathHelper.clamp((3000 - time) / 1000.0, 0, 1) * (this.client.currentScreen != null ? 90 : 130)), 255, 255, 255), false);
        }

        SpeedRunIGT.DEBUG_DATA = timer.getStatus().name();
        if (!this.client.options.hudHidden && this.client.world != null && timer.getStatus() != TimerStatus.NONE
                && (!this.client.isPaused() || this.client.currentScreen instanceof CreditsScreen || this.client.currentScreen instanceof GameMenuScreen || !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS))
                && !(!this.client.isPaused() && SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) && this.client.getDebugHud().shouldShowDebugHud())
                && !(this.client.currentScreen instanceof TimerCustomizeScreen)) {

            boolean needUpdate = SpeedRunIGTClient.TIMER_DRAWER.isNeedUpdate();
            boolean enableSplit = SpeedRunOption.getOption(SpeedRunOptions.ENABLE_TIMER_SPLIT_POS);
            if (needUpdate || enableSplit) {
                TimerDrawer.PositionType updatePositionType = TimerDrawer.PositionType.DEFAULT;
                if (enableSplit && this.client.getDebugHud().shouldShowDebugHud())
                    updatePositionType = TimerDrawer.PositionType.WHILE_F3;
                if (enableSplit && this.client.isPaused() && !(this.client.currentScreen instanceof DownloadingTerrainScreen) && (this.client.currentScreen instanceof GameMenuScreen && ((GameMenuScreenAccessor) this.client.currentScreen).isShowMenu()))
                    updatePositionType = TimerDrawer.PositionType.WHILE_PAUSED;

                if (currentPositionType != updatePositionType || needUpdate) {
                    currentPositionType = updatePositionType;
                    Vec2f igtPos = currentPositionType == TimerDrawer.PositionType.DEFAULT
                            ? new Vec2f(SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_X), SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_Y))
                            : SpeedRunOption.getOption(currentPositionType == TimerDrawer.PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_IGT_POSITION_FOR_F3 : SpeedRunOptions.TIMER_IGT_POSITION_FOR_PAUSE);

                    Vec2f rtaPos = currentPositionType == TimerDrawer.PositionType.DEFAULT
                            ? new Vec2f(SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_X), SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_Y))
                            : SpeedRunOption.getOption(currentPositionType == TimerDrawer.PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_RTA_POSITION_FOR_F3 : SpeedRunOptions.TIMER_RTA_POSITION_FOR_PAUSE);

                    SpeedRunIGTClient.TIMER_DRAWER.setRTA_XPos(rtaPos.x);
                    SpeedRunIGTClient.TIMER_DRAWER.setRTA_YPos(rtaPos.y);
                    SpeedRunIGTClient.TIMER_DRAWER.setIGT_XPos(igtPos.x);
                    SpeedRunIGTClient.TIMER_DRAWER.setIGT_YPos(igtPos.y);
                }
            }
            SpeedRunIGTClient.TIMER_DRAWER.draw(drawContext);
        }
    }

}
