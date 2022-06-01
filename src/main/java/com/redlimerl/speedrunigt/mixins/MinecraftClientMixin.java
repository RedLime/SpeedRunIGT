package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.gui.screen.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerClientUtils;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunType;
import com.redlimerl.speedrunigt.utils.MixinValues;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
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

    @Shadow @Nullable public ClientWorld world;

    @Shadow private boolean paused;

    private boolean disconnectCheck = false;

    @Inject(at = @At("HEAD"), method = "startGame")
    public void onCreate(String name, String displayName, LevelInfo levelInfo, CallbackInfo ci) {
        try {
            if (levelInfo != null) {
                InGameTimer.start(name, RunType.fromBoolean(InGameTimerUtils.IS_SET_SEED));
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
        disconnectCheck = false;
    }

    @Inject(method = "openScreen", at = @At("RETURN"))
    public void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof ProgressScreen) {
            disconnectCheck = true;
        }
        if (InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN != null) {
            Screen screen1 = InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN;
            InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN = null;
            MinecraftClient.getInstance().openScreen(screen1);
        }
    }

    @Inject(at = @At("HEAD"), method = "connect(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V")
    public void onJoin(ClientWorld targetWorld, String loadingMessage, CallbackInfo ci) {
        if (targetWorld == null) return;
        InGameTimer timer = InGameTimer.getInstance();

        InGameTimerUtils.IS_CHANGING_DIMENSION = false;

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.setPause(true, TimerStatus.IDLE, "changed dimension");
        }

        // For Timelines
        if (targetWorld.dimension.getType() == -1) {
            timer.tryInsertNewTimeline("enter_nether");
        } else if (targetWorld.dimension.getType() == 1) {
            timer.tryInsertNewTimeline("enter_end");
        }

        //Enter Nether
        if (timer.getCategory() == RunCategories.ENTER_NETHER && targetWorld.dimension.getType() == -1) {
            InGameTimer.complete();
        }

        //Enter End
        if (timer.getCategory() == RunCategories.ENTER_END && targetWorld.dimension.getType() == 1) {
            InGameTimer.complete();
        }

        RunCategories.checkAllBossesCompleted();
    }

    @Inject(method = "runGameLoop", at = @At("TAIL"))
    private void renderMixin(CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.RUNNING && this.paused) {
            timer.setPause(true, TimerStatus.PAUSED, "player");
            if (InGameTimerClientUtils.getGeneratedChunkRatio() < 0.1f) {
                InGameTimerUtils.RETIME_IS_WAITING_LOAD = true;
            }
        } else if (timer.getStatus() == TimerStatus.PAUSED && !this.paused) {
            timer.setPause(false, "player");
        }
    }


    @Inject(method = "runGameLoop", at = @At(value = "INVOKE",
            target ="Lnet/minecraft/client/render/GameRenderer;render(FJ)V", shift = At.Shift.AFTER))
    private void drawTimer(CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        if (InGameTimerClientUtils.canUnpauseTimer(true)) {
            if (!(InGameTimerUtils.isWaitingFirstInput() && !timer.isStarted())) {
                timer.setPause(false, "rendered");
            } else {
                timer.updateFirstRendered();
            }
        }

        SpeedRunIGT.DEBUG_DATA = timer.getStatus().name();
        if (!this.options.hudHidden && this.world != null && timer.getStatus() != TimerStatus.NONE
                && (!this.isPaused() || this.currentScreen instanceof CreditsScreen || this.currentScreen instanceof GameMenuScreen || !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS))
                && !(!this.isPaused() && SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) && this.options.debugEnabled)
                && !(this.currentScreen instanceof TimerCustomizeScreen)
                && MixinValues.IS_RENDERED_BEFORE) {
            SpeedRunIGTClient.TIMER_DRAWER.draw();
        }

        MixinValues.IS_RENDERED_BEFORE = false;
    }



    @Redirect(method="tick", at=@At(value="INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I", remap = false))
    public int getScrolled(){
        if(Mouse.getEventDWheel()!=0){
            unlock();
        }
        return Mouse.getEventDWheel();
    }

    @Inject(method="tick",at=@At(value = "HEAD"))
    public void getMoved(CallbackInfo ci){
        if(Mouse.getDX() != 0||Mouse.getDY() != 0){
            unlock();
        }
    }

    private void unlock() {
        InGameTimer timer = InGameTimer.getInstance();
        if (InGameTimerClientUtils.canUnpauseTimer(false)) {
            timer.setPause(false, "moved mouse");
        }
        if (Display.isActive() && !MinecraftClient.getInstance().isPaused() && Mouse.isGrabbed()) {
            timer.updateFirstInput();
        }
    }

    // Crash safety
    @Inject(method = "cleanHeap", at = @At("HEAD"))
    public void onCrash(CallbackInfo ci) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) InGameTimer.leave();
    }

    // Crash safety
    @Inject(method = "printCrashReport", at = @At("HEAD"))
    private void onCrash(CrashReport crashReport, CallbackInfo ci) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) InGameTimer.leave();
    }

    // Record save
    @Inject(method = "stop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;close()V", shift = At.Shift.BEFORE))
    public void onStop(CallbackInfo ci) {
        InGameTimer.getInstance().writeRecordFile(false);
    }

    // Disconnecting fix
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/ResourcePackLoader;method_7040()V", shift = At.Shift.BEFORE), method = "connect(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V")
    public void disconnect(CallbackInfo ci) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE && disconnectCheck) {
            InGameTimer.leave();
        }
        MixinValues.IS_CHANGED_WORLD = false;
    }
}
