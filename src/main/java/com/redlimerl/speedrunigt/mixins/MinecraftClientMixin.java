package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.gui.screen.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import net.minecraft.class_3793;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public GameOptions options;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Shadow public abstract boolean isPaused();

    @Shadow @Nullable public Screen currentScreen;

    @Shadow @Nullable public ClientWorld world;

    @Shadow @Final public Profiler profiler;

    @Shadow private boolean paused;

    @Inject(at = @At("HEAD"), method = "startGame")
    public void onCreate(String name, String displayName, LevelInfo levelInfo, CallbackInfo ci) {
        try {
            if (levelInfo != null) {
                InGameTimer.start(name);
            } else {
                boolean loaded = InGameTimer.load(name);
                if (!loaded) InGameTimer.end();
            }
        } catch (Exception e) {
            InGameTimer.end();
            SpeedRunIGT.error("Exception in timer load, can't load the timer.");
            e.printStackTrace();
        }
        InGameTimerUtils.IS_CHANGING_DIMENSION = true;
    }

    @Inject(at = @At("HEAD"), method = "connect")
    public void onJoin(ClientWorld targetWorld, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() == TimerStatus.NONE || targetWorld == null) return;

        InGameTimerUtils.IS_CHANGING_DIMENSION = false;

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.setPause(true, TimerStatus.IDLE, "changed dimension");
        }

        // For Timelines
        if (timer.getCategory() == RunCategories.ANY) {
            if (targetWorld.dimension.method_11789() == class_3793.field_18955) {
                timer.tryInsertNewTimeline("enter_nether");
            } else if (targetWorld.dimension.method_11789() == class_3793.field_18956) {
                timer.tryInsertNewTimeline("enter_end");
            }
        }

        //Enter Nether
        if (timer.getCategory() == RunCategories.ENTER_NETHER && targetWorld.dimension.method_11789() == class_3793.field_18955) {
            InGameTimer.complete();
            return;
        }

        //Enter End
        if (timer.getCategory() == RunCategories.ENTER_END && targetWorld.dimension.method_11789() == class_3793.field_18956) {
            InGameTimer.complete();
        }

        RunCategories.checkAllBossesCompleted();
    }

    @Inject(method = "method_18228", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;method_20230()J", shift = At.Shift.AFTER))
    private void renderMixin(boolean tick, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.RUNNING && this.paused) {
            timer.setPause(true, TimerStatus.PAUSED, "player");
        } else if (timer.getStatus() == TimerStatus.PAUSED && !this.paused) {
            timer.setPause(false, "player");
        }
    }


    @Inject(method = "method_18228", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_3264;method_18450()V", shift = At.Shift.AFTER))
    private void drawTimer(CallbackInfo ci) {
        this.profiler.swap("timer");
        InGameTimer timer = InGameTimer.getInstance();

        if (InGameTimerUtils.canUnpauseTimer(true)) {
            if (!(SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT) && !timer.isStarted())) {
                timer.setPause(false, "rendered");
            } else {
                timer.updateFirstRendered();
            }
        }

        SpeedRunIGT.DEBUG_DATA = timer.getStatus().name();
        if (!this.options.field_19987 && this.world != null && timer.getStatus() != TimerStatus.NONE
                && (!this.isPaused() || this.currentScreen instanceof CreditsScreen || this.currentScreen instanceof GameMenuScreen || !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS))
                && !(!this.isPaused() && SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) && this.options.debugEnabled)
                && !(this.currentScreen instanceof TimerCustomizeScreen)) {
            SpeedRunIGT.TIMER_DRAWER.draw();
        }
    }

    // Crash safety
    @Inject(method = "addSystemDetailsToCrashReport", at = @At("HEAD"))
    public void onCrash(CrashReport report, CallbackInfoReturnable<CrashReport> cir) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) InGameTimer.leave();
    }
}
