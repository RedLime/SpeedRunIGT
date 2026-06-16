package com.redlimerl.speedrunigt.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.gui.screen.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.mixins.access.PauseScreenAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.*;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GameRendererMixin {
    @Shadow @Final
    private Minecraft minecraft;
    @Unique
    private TimerDrawer.PositionType currentPositionType = TimerDrawer.PositionType.DEFAULT;
    @Inject(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/toasts/ToastManager;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", shift = At.Shift.AFTER))
    private void drawTimer(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo ci, @Local(name = "graphics") GuiGraphicsExtractor graphics) {
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
            graphics.text(this.minecraft.font, text, this.minecraft.gui.screen() != null ? (int) ((this.minecraft.getWindow().getGuiScaledWidth() - this.minecraft.font.width(text)) / 2f) : 4, this.minecraft.getWindow().getGuiScaledHeight() - 12,
                    ARGB.color((int) (Mth.clamp((3000 - time) / 1000.0, 0, 1) * (this.minecraft.gui.screen() != null ? 90 : 130)), 255, 255, 255), false);
        }

        SpeedRunIGT.DEBUG_DATA = timer.getStatus().name();
        if (!this.minecraft.gui.hud.isHidden() && this.minecraft.level != null && timer.getStatus() != TimerStatus.NONE
                && (!this.minecraft.isPaused() || this.minecraft.gui.screen() instanceof WinScreen || this.minecraft.gui.screen() instanceof PauseScreen || !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS))
                && !(!this.minecraft.isPaused() && SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) && this.minecraft.getDebugOverlay().showDebugScreen())
                && !(this.minecraft.gui.screen() instanceof TimerCustomizeScreen)) {

            boolean needUpdate = SpeedRunIGTClient.TIMER_DRAWER.isNeedUpdate();
            boolean enableSplit = SpeedRunOption.getOption(SpeedRunOptions.ENABLE_TIMER_SPLIT_POS);
            if (needUpdate || enableSplit) {
                TimerDrawer.PositionType updatePositionType = TimerDrawer.PositionType.DEFAULT;
                if (enableSplit && this.minecraft.getDebugOverlay().showDebugScreen())
                    updatePositionType = TimerDrawer.PositionType.WHILE_F3;
                if (enableSplit && this.minecraft.isPaused() && !(this.minecraft.gui.screen() instanceof LevelLoadingScreen) && (this.minecraft.gui.screen() instanceof PauseScreen && ((PauseScreenAccessor) this.minecraft.gui.screen()).isShowPauseMenu()))
                    updatePositionType = TimerDrawer.PositionType.WHILE_PAUSED;

                if (currentPositionType != updatePositionType || needUpdate) {
                    currentPositionType = updatePositionType;
                    Vec2 igtPos = currentPositionType == TimerDrawer.PositionType.DEFAULT
                            ? new Vec2(SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_X), SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_Y))
                            : SpeedRunOption.getOption(currentPositionType == TimerDrawer.PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_IGT_POSITION_FOR_F3 : SpeedRunOptions.TIMER_IGT_POSITION_FOR_PAUSE);

                    Vec2 rtaPos = currentPositionType == TimerDrawer.PositionType.DEFAULT
                            ? new Vec2(SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_X), SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_Y))
                            : SpeedRunOption.getOption(currentPositionType == TimerDrawer.PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_RTA_POSITION_FOR_F3 : SpeedRunOptions.TIMER_RTA_POSITION_FOR_PAUSE);

                    SpeedRunIGTClient.TIMER_DRAWER.setRTA_XPos(rtaPos.x);
                    SpeedRunIGTClient.TIMER_DRAWER.setRTA_YPos(rtaPos.y);
                    SpeedRunIGTClient.TIMER_DRAWER.setIGT_XPos(igtPos.x);
                    SpeedRunIGTClient.TIMER_DRAWER.setIGT_YPos(igtPos.y);
                }
            }
            SpeedRunIGTClient.TIMER_DRAWER.draw(graphics);
        }
    }

    @Inject(method = "setScreen", at = @At("RETURN"))
    public void onSetScreenAndShow(Screen screen, CallbackInfo ci) {
        if (screen instanceof LevelLoadingScreen) {
            InGameTimerUtils.CAN_DISCONNECT = true;
        }
        if (InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN != null) {
            Screen screen1 = InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN;
            InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN = null;
            Minecraft.getInstance().gui.setScreen(screen1);
        }
    }


}
