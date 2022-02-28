package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.gui.screen.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.mixins.access.WorldRendererAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import net.minecraft.class_3793;
import net.minecraft.class_4112;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.WorldRenderer;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public GameOptions options;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Shadow public abstract boolean isPaused();

    @Shadow @Nullable public Screen currentScreen;

    @Shadow public WorldRenderer worldRenderer;

    @Shadow @Nullable public ClientWorld world;

    @Shadow public abstract boolean isWindowFocused();

    @Shadow @Final public Profiler profiler;

    @Shadow public class_4112 field_19945;

    @Inject(at = @At("HEAD"), method = "startGame")
    public void onCreate(String name, String displayName, LevelInfo levelInfo, CallbackInfo ci) {
        try {
            if (levelInfo != null) {
                InGameTimer.start();
                currentDimension = null;
                InGameTimer.currentWorldName = name;
            } else {
                boolean loaded = InGameTimer.load(name);
                if (!loaded) InGameTimer.end();
                else {
                    InGameTimer.currentWorldName = name;
                }
                currentDimension = null;
            }
        } catch (Exception e) {
            InGameTimer.end();
            currentDimension = null;
            SpeedRunIGT.error("Exception in timer load, can't load the timer.");
            e.printStackTrace();
        }
    }

    private static class_3793 currentDimension = null;

    @Inject(at = @At("HEAD"), method = "connect")
    public void onJoin(ClientWorld targetWorld, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() == TimerStatus.NONE || targetWorld == null) return;

        currentDimension = targetWorld.dimension.method_11789();
        InGameTimer.checkingWorld = true;

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.setPause(true, TimerStatus.IDLE);
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
    }

    @ModifyVariable(method = "method_18228", at = @At(value = "STORE"), ordinal = 1)
    private boolean renderMixin(boolean paused) {
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.RUNNING && paused) {
            timer.setPause(true, TimerStatus.PAUSED);
        } else if (timer.getStatus() == TimerStatus.PAUSED && !paused) {
            timer.setPause(false);
        }

        return paused;
    }


    @Inject(method = "method_18228", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_3264;method_18450()V", shift = At.Shift.AFTER))
    private void drawTimer(CallbackInfo ci) {
        this.profiler.swap("timer");
        InGameTimer timer = InGameTimer.getInstance();

        if (worldRenderer != null && world != null && world.dimension.method_11789() == currentDimension && !isPaused() && isWindowFocused()
                && timer.getStatus() == TimerStatus.IDLE && InGameTimer.checkingWorld && this.field_19945.method_18252()) {
            WorldRendererAccessor worldRendererAccessor = (WorldRendererAccessor) worldRenderer;
            int chunks = worldRendererAccessor.invokeCompletedChunkCount();
            int entities = worldRendererAccessor.getRegularEntityCount() - (options.perspective > 0 ? 0 : 1);

            if (chunks + entities > 0) {
                if (!(SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT) && !timer.isStarted())) {
                    timer.setPause(false);
                } else {
                    timer.updateFirstRendered();
                }
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
