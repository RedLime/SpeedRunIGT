package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.MouseInput;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ChunkAssemblyHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public abstract boolean isInSingleplayer();

    @Shadow
    public GameOptions options;
    private float previousX=0;
    private float previousY=0;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Shadow public abstract boolean isPaused();

    @Shadow @Nullable public Screen currentScreen;

    @Shadow
    public WorldRenderer worldRenderer;

    @Shadow @Nullable public ClientWorld world;


    @Shadow private IntegratedServer server;

    @Shadow public boolean focused;

    @Shadow public MouseInput mouse;
    @Shadow @Final public Profiler profiler;

    /**
     * @author Void_X_Walker
     * @reason Backported to 1.8, merged the 1.16 methods: startIntegratedServer and method_29607 and used levelInfo == null as a distinction e
     */
    @Inject(at = @At("HEAD"), method = "startGame")
    public void onCreate(String worldName, String string, LevelInfo levelInfo, CallbackInfo ci) {
        InGameTimer.start();
        currentDimension = null;
        InGameTimer.currentWorldName = worldName;
        if(levelInfo==null){
            try {
                boolean loaded = InGameTimer.load(worldName);
                if (!loaded) InGameTimer.end();
                else {
                    InGameTimer.currentWorldName = worldName;
                }
            } catch (Exception e) {
                InGameTimer.end();
            }
            currentDimension = null;
        }

    }



    private static Dimension currentDimension = null;
    /**
     * @author Void_X_Walker
     * @reason Backported to 1.8
     */
    @Inject(at = @At("HEAD"), method = "connect(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V")
    public void onJoin(ClientWorld targetWorld, String loadingMessage, CallbackInfo ci) {
        if (!isInSingleplayer()||targetWorld==null) return;
        InGameTimer timer = InGameTimer.getInstance();

        currentDimension = targetWorld.dimension;
        InGameTimer.checkingWorld = true;

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.setPause(true, TimerStatus.IDLE);
        }

        //Enter Nether
        if (timer.getCategory() == RunCategory.ENTER_NETHER && targetWorld.dimension.isNether()) {
            InGameTimer.complete();
        }

        //Enter End
        if (timer.getCategory() == RunCategory.ENTER_END && !targetWorld.dimension.hasGround()) {
            InGameTimer.complete();
        }
    }
    /**
     * @author Void_X_Walker
     * @reason Backported to 1.8, moved the custom keybinds here so there is no need for fabric api
     */
    @Redirect(method="runGameLoop",at=@At(value="INVOKE",target = "Lnet/minecraft/client/MinecraftClient;isInSingleplayer()Z"))
    private boolean renderMixin(MinecraftClient instance) {
        if(Keyboard.isKeyDown(22)){//U
            InGameTimer timer = InGameTimer.getInstance();
            if (timer.getCategory() == RunCategory.CUSTOM && timer.isResettable()) {
                InGameTimer.reset();
            }
        }
        if(Keyboard.isKeyDown(23)){//I
            InGameTimer timer = InGameTimer.getInstance();
            if (timer.getCategory() == RunCategory.CUSTOM && timer.isStarted()) {
                InGameTimer.complete();
            }

        }
        InGameTimer timer = InGameTimer.getInstance();
        boolean paused = this.isInSingleplayer() && this.currentScreen != null && this.currentScreen.shouldPauseGame() && !this.server.isPublished();
        if (timer.getStatus() == TimerStatus.RUNNING && paused) {
            timer.setPause(true, TimerStatus.PAUSED);
        } else if (timer.getStatus() == TimerStatus.PAUSED && !paused) {
            timer.setPause(false);
        }

        return this.isInSingleplayer();
    }
    /**
     * @author Void_X_Walker
     * @reason Backported to 1.8, merged Inject and ModifyVariable, created a custom getCompletedChunkCount method with an access Widener
     */

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE",
            target ="Lnet/minecraft/client/render/GameRenderer;render(FJ)V", shift = At.Shift.AFTER))
    private void drawTimer(CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

            if (worldRenderer != null && world != null && world.dimension.getName().equals(currentDimension.getName()) && !isPaused()
                    && (timer.getStatus() == TimerStatus.IDLE ) && InGameTimer.checkingWorld && Mouse.isGrabbed()) {
                int chunks = getCompletedChunkCount();
                int entities = worldRenderer.field_1891 - (options.perspective > 0 ? 0 : 1);
                if (chunks + entities > 0) {
                    if (!(SpeedRunOptions.getOption(SpeedRunOptions.WAITING_FIRST_INPUT) && !timer.isStarted())) {
                        timer.setPause(false);
                    } else {
                        timer.updateFirstRendered();
                    }
                }
        }


        SpeedRunIGT.DEBUG_DATA = timer.getStatus().name();

        if (!this.options.hudHidden && this.world != null && timer.getStatus() != TimerStatus.NONE
                && (!this.isPaused() || this.currentScreen instanceof CreditsScreen || this.currentScreen instanceof GameMenuScreen || !SpeedRunOptions.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS))
                && !(!this.isPaused() && SpeedRunOptions.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) && this.options.debugEnabled)
                && !(this.currentScreen instanceof TimerCustomizeScreen)) {
            this.profiler.swap("timer");
            SpeedRunIGT.TIMER_DRAWER.draw();
        }
    }
    private int getCompletedChunkCount(){
        int j = 0;

        for (WorldRenderer.ChunkInfo chunkInfo : worldRenderer.visibleChunks) {
            ChunkAssemblyHelper chunkAssemblyHelper = chunkInfo.field_10830.field_11070;
            if (chunkAssemblyHelper != ChunkAssemblyHelper.UNSUPPORTED && !chunkAssemblyHelper.method_10142()) {
                ++j;
            }
        }
        return j;
    }
    /**
     * @author Void_X_Walker
     * @reason Backported to 1.8, Moved the mouse stuff from MouseMixin and redid it
     */

    @Redirect(method="tick",at=@At(value="INVOKE",target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I"))
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
        @NotNull
        InGameTimer timer = InGameTimer.getInstance();
        if ((timer.getStatus() == TimerStatus.IDLE )  &&this.focused&& !MinecraftClient.getInstance().isPaused() && InGameTimer.checkingWorld && Mouse.isGrabbed()) {
            timer.setPause(false);
        }
        if (this.focused&&!MinecraftClient.getInstance().isPaused() && Mouse.isGrabbed()) {
            timer.updateFirstInput();
        }
    }
}
