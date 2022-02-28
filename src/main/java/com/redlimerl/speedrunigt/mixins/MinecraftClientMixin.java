package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.gui.screen.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.mixins.access.WorldRendererAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.TheEndDimension;
import net.minecraft.world.dimension.TheNetherDimension;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Shadow public boolean focused;

    @Shadow private boolean paused;

    @Shadow @Final public Profiler profiler;

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

    private static Dimension currentDimension = null;

    @Inject(at = @At("HEAD"), method = "connect(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V")
    public void onJoin(ClientWorld targetWorld, String loadingMessage, CallbackInfo ci) {
        if (targetWorld == null) return;
        InGameTimer timer = InGameTimer.getInstance();

        currentDimension = targetWorld.dimension;
        InGameTimer.checkingWorld = true;

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.setPause(true, TimerStatus.IDLE);
        }

        //Enter Nether
        if (timer.getCategory() == RunCategories.ENTER_NETHER && targetWorld.dimension instanceof TheNetherDimension) {
            InGameTimer.complete();
        }

        //Enter End
        if (timer.getCategory() == RunCategories.ENTER_END && targetWorld.dimension instanceof TheEndDimension) {
            InGameTimer.complete();
        }
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Ljava/lang/System;nanoTime()J", shift = At.Shift.BEFORE))
    private void renderMixin(CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.RUNNING && this.paused) {
            timer.setPause(true, TimerStatus.PAUSED);
        } else if (timer.getStatus() == TimerStatus.PAUSED && !paused) {
            timer.setPause(false);
        }
    }


    @Inject(method = "runGameLoop", at = @At(value = "INVOKE",
            target ="Lnet/minecraft/client/render/GameRenderer;method_1331(F)V", shift = At.Shift.AFTER))
    private void drawTimer(CallbackInfo ci) {
        this.profiler.swap("timer");
        InGameTimer timer = InGameTimer.getInstance();

        if (worldRenderer != null && world != null && currentDimension != null && world.dimension.getName().equals(currentDimension.getName()) && !isPaused()
                && (timer.getStatus() == TimerStatus.IDLE ) && InGameTimer.checkingWorld && Mouse.isGrabbed() && Display.isActive() && Mouse.isInsideWindow()) {
            WorldRendererAccessor worldRendererAccessor = (WorldRendererAccessor) worldRenderer;
            int chunks = worldRendererAccessor.getCompletedChunkCount();
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
        if (!this.options.hudHidden && this.world != null && timer.getStatus() != TimerStatus.NONE
                && (!this.isPaused() || this.currentScreen instanceof CreditsScreen || this.currentScreen instanceof GameMenuScreen || !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS))
                && !(!this.isPaused() && SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) && this.options.debugEnabled)
                && !(this.currentScreen instanceof TimerCustomizeScreen)) {
            SpeedRunIGT.TIMER_DRAWER.draw();
        }
    }




    /**
     * Moved the mouse stuff from MouseMixin and redid it by Void_X_Walker
     */
    private float previousX=0;
    private float previousY=0;

    @Redirect(method="tick", at=@At(value="INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I"))
    public int getScrolled(){
        if(Mouse.getEventDWheel()!=0){
            unlock();
        }
        return Mouse.getEventDWheel();
    }

    @Inject(method="tick",at=@At(value = "HEAD"))
    public void getMoved(CallbackInfo ci){
        if(Mouse.getX()!=previousX||Mouse.getY()!=previousY){
            unlock();
        }
        previousX=Mouse.getX();
        previousY=Mouse.getY();
    }

    private void unlock() {
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() == TimerStatus.COMPLETED_LEGACY || timer.getStatus() == TimerStatus.NONE) return;

        if ((timer.getStatus() == TimerStatus.IDLE )  && Display.isActive() && !MinecraftClient.getInstance().isPaused() && InGameTimer.checkingWorld && Mouse.isGrabbed()) {
            timer.setPause(false);
        }
        if (this.focused&&!MinecraftClient.getInstance().isPaused() && Mouse.isGrabbed()) {
            timer.updateFirstInput();
        }
    }

    // Crash safety
    @Inject(method = "addSystemDetailsToCrashReport", at = @At("HEAD"))
    public void onCrash(CrashReport report, CallbackInfoReturnable<CrashReport> cir) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) InGameTimer.leave();
    }
}
