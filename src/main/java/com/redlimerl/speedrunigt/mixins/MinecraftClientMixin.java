package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.gui.screen.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.instance.GameInstance;
import com.redlimerl.speedrunigt.mixins.access.FontManagerAccessor;
import com.redlimerl.speedrunigt.mixins.access.MinecraftClientAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.*;
import com.redlimerl.speedrunigt.timer.TimerDrawer.PositionType;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.running.RunType;
import com.redlimerl.speedrunigt.utils.FontUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow @Final public GameOptions options;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Shadow public abstract boolean isPaused();

    @Shadow @Nullable public Screen currentScreen;

    @Shadow @Nullable public ClientWorld world;

    @Shadow @Final private ReloadableResourceManager resourceManager;

    @Shadow private boolean paused;

    @Shadow @Final public TextRenderer textRenderer;
    @Shadow @Final private Window window;
    private boolean disconnectCheck = false;

    @Inject(at = @At("HEAD"), method = "createWorld")
    public void onCreate(String worldName, LevelInfo levelInfo, RegistryTracker.Modifiable registryTracker, GeneratorOptions generatorOptions, CallbackInfo ci) {
        // don't start timer when the world is being created on another thread
        // this is for compatibility with SeedQueue, where this is method is called to create background seeds
        if (!MinecraftClient.getInstance().isOnThread()) {
            return;
        }

        RunCategory category = SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY);
        if (category.isAutoStart()) InGameTimer.start(worldName, RunType.fromBoolean(InGameTimerUtils.IS_SET_SEED));
        InGameTimer.getInstance().setDefaultGameMode(levelInfo.getGameMode().getId());
        InGameTimer.getInstance().setCheatAvailable(levelInfo.areCommandsAllowed());
        InGameTimer.getInstance().checkDifficulty(levelInfo.getDifficulty());
        InGameTimerUtils.IS_CHANGING_DIMENSION = true;
        this.disconnectCheck = false;
    }

    @Inject(at = @At("HEAD"), method = "startIntegratedServer(Ljava/lang/String;)V")
    public void onWorldOpen(String worldName, CallbackInfo ci) {
        try {
            boolean loaded = InGameTimer.load(worldName);
            if (!loaded) InGameTimer.end();
        } catch (Exception e) {
            InGameTimer.end();
            SpeedRunIGT.error("Exception in timer load, can't load the timer.");
            e.printStackTrace();
        }
        InGameTimerUtils.IS_CHANGING_DIMENSION = true;
        this.disconnectCheck = false;
    }

    @Inject(method = "openScreen", at = @At("RETURN"))
    public void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof LevelLoadingScreen) {
            this.disconnectCheck = true;
        }
        if (InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN != null) {
            Screen screen1 = InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN;
            InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN = null;
            MinecraftClient.getInstance().openScreen(screen1);
        }
    }

    @Inject(at = @At("HEAD"), method = "joinWorld")
    public void onJoin(ClientWorld targetWorld, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() == TimerStatus.NONE) return;

        InGameTimerUtils.IS_CHANGING_DIMENSION = false;
        timer.setPause(true, TimerStatus.IDLE, "changed dimension");

        // For Timelines
        if (targetWorld.getDimensionRegistryKey() == DimensionType.THE_NETHER_REGISTRY_KEY) {
            timer.tryInsertNewTimeline("enter_nether");
        } else if (targetWorld.getDimensionRegistryKey() == DimensionType.THE_END_REGISTRY_KEY) {
            timer.tryInsertNewTimeline("enter_end");
        }

        //Enter Nether
        if (timer.getCategory() == RunCategories.ENTER_NETHER && targetWorld.getDimensionRegistryKey() == DimensionType.THE_NETHER_REGISTRY_KEY) {
            InGameTimer.complete();
            return;
        }

        //Enter End
        if (timer.getCategory() == RunCategories.ENTER_END && targetWorld.getDimensionRegistryKey() == DimensionType.THE_END_REGISTRY_KEY) {
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

    @Inject(method = "render(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeNano()J", shift = At.Shift.AFTER))
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
    private boolean previousNoUI = false;
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/util/math/MatrixStack;)V", shift = At.Shift.AFTER))
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
            String text = "SpeedRunIGT v" + (SpeedRunIGT.MOD_VERSION.split("\\+")[0]);
            this.textRenderer.draw(new MatrixStack(), text, this.currentScreen != null ? ((this.window.getScaledWidth() - this.textRenderer.getWidth(text)) / 2f) : 4, this.window.getScaledHeight() - 12,
                    BackgroundHelper.ColorMixer.getArgb((int) (MathHelper.clamp((3000 - time) / 1000.0, 0, 1) * (this.currentScreen != null ? 90 : 130)), 255, 255, 255));
        }

        SpeedRunIGT.DEBUG_DATA = timer.getStatus().name();
        if (!this.options.hudHidden && this.world != null && timer.getStatus() != TimerStatus.NONE
                && (!this.isPaused() || this.currentScreen instanceof CreditsScreen || this.currentScreen instanceof GameMenuScreen || !SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS))
                && !(!this.isPaused() && SpeedRunOption.getOption(SpeedRunOptions.HIDE_TIMER_IN_DEBUGS) && this.options.debugEnabled)
                && !(this.currentScreen instanceof TimerCustomizeScreen)) {

            boolean needUpdate = SpeedRunIGTClient.TIMER_DRAWER.isNeedUpdate();
            boolean enableSplit = SpeedRunOption.getOption(SpeedRunOptions.ENABLE_TIMER_SPLIT_POS);
            if (needUpdate || enableSplit) {
                PositionType updatePositionType = PositionType.DEFAULT;
                if (enableSplit && this.options.debugEnabled)
                    updatePositionType = PositionType.WHILE_F3;
                if (enableSplit && this.isPaused() && !(this.currentScreen instanceof DownloadingTerrainScreen) && (this.currentScreen instanceof GameMenuScreen && !this.previousNoUI))
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

    @Inject(method = "openPauseMenu", at = @At("HEAD"))
    public void onOpenPauseMenu(boolean pause, CallbackInfo ci) {
        this.previousNoUI = pause;
    }


    /**
     * Add import font system
     */
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/debug/DebugRenderer;<init>(Lnet/minecraft/client/MinecraftClient;)V", shift = At.Shift.BEFORE))
    public void onInit(RunArgs args, CallbackInfo ci) {
        this.resourceManager.registerListener(new SinglePreparationResourceReloadListener<Map<Identifier, List<Font>>>() {
            @Override
            protected Map<Identifier, List<Font>> prepare(ResourceManager manager, Profiler profiler) {
                SpeedRunIGT.FONT_MAPS.clear();

                HashMap<Identifier, List<Font>> map = new HashMap<>();

                File[] fontFiles = SpeedRunIGT.FONT_PATH.toFile().listFiles();
                if (fontFiles == null) return new HashMap<>();

                for (File file : Arrays.stream(fontFiles).filter(file -> file.getName().endsWith(".ttf") || file.getName().endsWith(".otf")).collect(Collectors.toList())) {
                    try {
                        File config = SpeedRunIGT.FONT_PATH.resolve(file.getName().substring(0, file.getName().length() - 4) + ".json").toFile();
                        if (config.exists()) {
                            FontUtils.addFont(map, file, config);
                        } else {
                            FontUtils.addFont(map, file, null);
                        }
                    } catch (Throwable e) {
                        SpeedRunIGT.error("Failed to load "+file.getName()+" font file");
                        e.printStackTrace();
                    }
                }
                return map;
            }

            @Override
            protected void apply(Map<Identifier, List<Font>> loader, ResourceManager manager, Profiler profiler) {
                try {
                    FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftClientAccessor) MinecraftClient.getInstance()).getFontManager();
                    for (Map.Entry<Identifier, List<Font>> listEntry : loader.entrySet()) {
                        FontStorage fontStorage = new FontStorage(fontManager.getTextureManager(), listEntry.getKey());
                        fontStorage.setFonts(listEntry.getValue());
                        fontManager.getFontStorages().put(listEntry.getKey(), fontStorage);
                    }
                    TimerDrawer.fontHeightMap.clear();
                } catch (Throwable e) {
                    SpeedRunIGT.error("Error! failed import timer fonts!");
                    e.printStackTrace();
                }
            }
        });
    }

    // Crash safety
    @Inject(method = "cleanUpAfterCrash", at = @At("HEAD"))
    public void onCrash(CallbackInfo ci) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) InGameTimer.leave();
    }

    // Crash safety
    @Inject(method = "printCrashReport", at = @At("HEAD"))
    private static void onCrash(CrashReport crashReport, CallbackInfo ci) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) InGameTimer.leave();
    }

    // Record save
    @Inject(method = "stop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;close()V", shift = At.Shift.BEFORE))
    public void onStop(CallbackInfo ci) {
        InGameTimer.getInstance().writeRecordFile(false);
    }

    // Disconnecting fix
    @Inject(at = @At("HEAD"), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
    public void disconnect(CallbackInfo ci) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE && this.disconnectCheck) {
            GameInstance.getInstance().callEvents("leave_world");
            InGameTimer.leave();
        }
    }
}
