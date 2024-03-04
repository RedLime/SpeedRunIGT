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
import com.redlimerl.speedrunigt.version.ColorMixer;
import net.minecraft.class_4117;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.Nullable;
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
    @Shadow public class_4117 field_19944;
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
        if (screen instanceof ProgressScreen) {
            disconnectCheck = true;
        }
        if (InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN != null) {
            Screen screen1 = InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN;
            InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN = null;
            MinecraftClient.getInstance().setScreen(screen1);
        }
    }

    @Inject(at = @At("HEAD"), method = "connect")
    public void onJoin(ClientWorld targetWorld, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() == TimerStatus.NONE || targetWorld == null) return;

        InGameTimerUtils.IS_CHANGING_DIMENSION = false;
        timer.setPause(true, TimerStatus.IDLE, "changed dimension");

        // For Timelines
        if (targetWorld.dimension.method_11789() == DimensionType.THE_NETHER) {
            timer.tryInsertNewTimeline("enter_nether");
        } else if (targetWorld.dimension.method_11789() == DimensionType.THE_END) {
            timer.tryInsertNewTimeline("enter_end");
        }

        //Enter Nether
        if (timer.getCategory() == RunCategories.ENTER_NETHER && targetWorld.dimension.method_11789() == DimensionType.THE_NETHER) {
            InGameTimer.complete();
            return;
        }

        //Enter End
        if (timer.getCategory() == RunCategories.ENTER_END && targetWorld.dimension.method_11789() == DimensionType.THE_END) {
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

    @Inject(method = "method_18228", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;method_20230()J", shift = At.Shift.AFTER))
    private void renderMixin(boolean tick, CallbackInfo ci) {
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
    @Inject(method = "method_18228", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_3264;method_18450()V", shift = At.Shift.AFTER))
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
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            String text = "SpeedRunIGT v" + (SpeedRunIGT.MOD_VERSION.split("\\+")[0]);
            this.textRenderer.method_18355(text, this.currentScreen != null ? ((this.field_19944.method_18321() - this.textRenderer.getStringWidth(text)) / 2f) : 4, this.field_19944.method_18322() - 12,
                    ColorMixer.getArgb((int) (MathHelper.clamp((3000 - time) / 1000.0, 0, 1) * (this.currentScreen != null ? 90 : 130)), 255, 255, 255));
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        SpeedRunIGT.DEBUG_DATA = timer.getStatus().name();
        if (!this.options.field_19987 && this.world != null && timer.getStatus() != TimerStatus.NONE
                && (!this.isPaused() || this.currentScreen instanceof CreditsScreen || this.currentScreen instanceof GameMenuScreen || !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS))
                && !(!this.isPaused() && SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) && this.options.debugEnabled)
                && !(this.currentScreen instanceof TimerCustomizeScreen)) {

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
    @Inject(method = "stop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/SpriteAtlasTexture;method_19516()V", shift = At.Shift.BEFORE))
    public void onStop(CallbackInfo ci) {
        InGameTimer.getInstance().writeRecordFile(false);
    }

    // Disconnecting fix
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/ResourcePackLoader;clear()V", shift = At.Shift.BEFORE), method = "method_18206")
    public void disconnect(CallbackInfo ci) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE && this.disconnectCheck) {
            GameInstance.getInstance().callEvents("leave_world");
            InGameTimer.leave();
        }
    }
}
