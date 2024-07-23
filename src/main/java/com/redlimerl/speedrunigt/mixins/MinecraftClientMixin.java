package com.redlimerl.speedrunigt.mixins;

import com.mojang.blaze3d.platform.GlStateManager;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.gui.screen.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.instance.GameInstance;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerClientUtils;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerDrawer.PositionType;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.running.RunType;
import com.redlimerl.speedrunigt.utils.MixinValues;
import com.redlimerl.speedrunigt.utils.Vec2f;
import com.redlimerl.speedrunigt.version.ColorMixer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public GameOptions options;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Shadow public abstract boolean isPaused();

    @Shadow @Nullable public Screen currentScreen;

    @Shadow @Nullable public ClientWorld world;

    @Shadow private boolean paused;

    @Shadow public TextRenderer textRenderer;
    @Shadow public int width;
    @Shadow public int height;
    @Shadow public boolean skipGameRender;
    private boolean disconnectCheck = false;

    @Inject(at = @At("HEAD"), method = "startIntegratedServer")
    public void onCreate(String name, String displayName, LevelInfo levelInfo, CallbackInfo ci) {
        try {
            if (levelInfo != null) {
                RunCategory category = SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY);
                if (category.isAutoStart()) {
                    InGameTimer.start(name, RunType.fromBoolean(InGameTimerUtils.IS_SET_SEED));
                    InGameTimer.getInstance().setDefaultGameMode(levelInfo.method_3758().getGameModeId());
                    InGameTimer.getInstance().setCheatAvailable(levelInfo.allowCommands());
                }
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
        this.disconnectCheck = false;
    }

    @Inject(method = "setScreen", at = @At("RETURN"))
    public void onSetScreen(Screen screen, CallbackInfo ci) {
        if (InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN != null) {
            Screen screen1 = InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN;
            InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN = null;
            MinecraftClient.getInstance().setScreen(screen1);
        }
    }

    @Inject(at = @At("HEAD"), method = "connect(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V")
    public void onJoin(ClientWorld targetWorld, String loadingMessage, CallbackInfo ci) {
        if (targetWorld == null) return;
        InGameTimer timer = InGameTimer.getInstance();

        InGameTimerUtils.IS_CHANGING_DIMENSION = false;
        timer.setPause(true, TimerStatus.IDLE, "changed dimension");

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

    private int saveTickCount = 0;
    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickMixin(CallbackInfo ci) {
        if (++this.saveTickCount >= 20) {
            SpeedRunOption.checkSave();
            this.saveTickCount = 0;
        }
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

    private PositionType currentPositionType = PositionType.DEFAULT;
    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/AchievementNotification;tick()V", shift = At.Shift.AFTER))
    private void drawTimer(CallbackInfo ci) {
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
            Window window = new Window((MinecraftClient) ((Object) this), this.width, this.height);
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            String text = "SpeedRunIGT v" + (SpeedRunIGT.MOD_VERSION.split("\\+")[0]);
            this.textRenderer.draw(text, this.currentScreen != null ? (int) ((window.getScaledWidth() - this.textRenderer.getStringWidth(text)) / 2f) : 4, (int) window.getScaledHeight() - 12,
                    ColorMixer.getArgb((int) (MathHelper.clamp((3000 - time) / 1000.0, 0, 1) * (this.currentScreen != null ? 90 : 130)), 255, 255, 255));
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        SpeedRunIGT.DEBUG_DATA = timer.getStatus().name();
        if (!this.options.hudHidden && this.world != null && timer.getStatus() != TimerStatus.NONE
                && (!this.isPaused() || this.currentScreen instanceof CreditsScreen || this.currentScreen instanceof GameMenuScreen || !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS))
                && !(!this.isPaused() && SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) && this.options.debugEnabled)
                && !(this.currentScreen instanceof TimerCustomizeScreen)
                && MixinValues.IS_RENDERED_BEFORE
                && !this.skipGameRender) {
            boolean needUpdate = SpeedRunIGTClient.TIMER_DRAWER.isNeedUpdate();
            boolean enableSplit = SpeedRunOption.getOption(SpeedRunOptions.ENABLE_TIMER_SPLIT_POS);
            if (needUpdate || enableSplit) {
                PositionType updatePositionType = PositionType.DEFAULT;
                if (enableSplit && this.options.debugEnabled)
                    updatePositionType = PositionType.WHILE_F3;
                if (enableSplit && this.isPaused() && !(this.currentScreen instanceof DownloadingTerrainScreen))
                    updatePositionType = PositionType.WHILE_PAUSED;

                if (this.currentPositionType != updatePositionType || needUpdate) {
                    this.currentPositionType = updatePositionType;
                    Vec2f igtPos = this.currentPositionType == PositionType.DEFAULT
                            ? new Vec2f(SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_X), SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_Y))
                            : SpeedRunOption.getOption(this.currentPositionType == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_IGT_POSITION_FOR_F3 : SpeedRunOptions.TIMER_IGT_POSITION_FOR_PAUSE);

                    Vec2f rtaPos = this.currentPositionType == PositionType.DEFAULT
                            ? new Vec2f(SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_X), SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_Y))
                            : SpeedRunOption.getOption(this.currentPositionType == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_RTA_POSITION_FOR_F3 : SpeedRunOptions.TIMER_RTA_POSITION_FOR_PAUSE);

                    SpeedRunIGTClient.TIMER_DRAWER.setRTA_XPos(rtaPos.x);
                    SpeedRunIGTClient.TIMER_DRAWER.setRTA_YPos(rtaPos.y);
                    SpeedRunIGTClient.TIMER_DRAWER.setIGT_XPos(igtPos.x);
                    SpeedRunIGTClient.TIMER_DRAWER.setIGT_YPos(igtPos.y);
                }
            }
            SpeedRunIGTClient.TIMER_DRAWER.draw();
        }

        MixinValues.IS_RENDERED_BEFORE = false;

        if (this.world != null)
            disconnectCheck = true;
    }



    @Inject(method="tick", at=@At(value="INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I", remap = false))
    public void getScrolled(CallbackInfo ci){
        if(Mouse.getEventDWheel()!=0){
            unlock();
        }
    }

    private void unlock() {
        InGameTimer timer = InGameTimer.getInstance();
        if (InGameTimerClientUtils.canUnpauseTimer(false)) {
            timer.setPause(false, "moved mouse wheel");
        }
        if (Display.isActive() && !MinecraftClient.getInstance().isPaused() && Mouse.isGrabbed()) {
            timer.updateFirstInput();
        }
    }

    // Crash safety
    @Inject(method = "cleanUpAfterCrash", at = @At("HEAD"))
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
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/ResourcePackLoader;getServerContainer()Lnet/minecraft/resource/ResourcePack;", shift = At.Shift.BEFORE), method = "connect(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V")
    public void disconnect(CallbackInfo ci) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE && this.disconnectCheck) {
            GameInstance.getInstance().callEvents("leave_world");
            InGameTimer.leave();
        }
        MixinValues.IS_CHANGED_WORLD = false;
    }
}
