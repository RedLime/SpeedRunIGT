package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.gui.screen.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.mixins.access.FontManagerAccessor;
import com.redlimerl.speedrunigt.mixins.access.MinecraftClientAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.*;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunType;
import com.redlimerl.speedrunigt.utils.FontUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    private boolean disconnectCheck = false;

    @Inject(at = @At("HEAD"), method = "startIntegratedServer")
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
        if (screen instanceof LevelLoadingScreen) {
            disconnectCheck = true;
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

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.setPause(true, TimerStatus.IDLE, "changed dimension");
        }

        // For Timelines
        if (timer.getCategory() == RunCategories.ANY) {
            if (targetWorld.getDimension().getType() == DimensionType.THE_NETHER) {
                timer.tryInsertNewTimeline("enter_nether");
            } else if (targetWorld.getDimension().getType() == DimensionType.THE_END) {
                timer.tryInsertNewTimeline("enter_end");
            }
        }

        //Enter Nether
        if (timer.getCategory() == RunCategories.ENTER_NETHER && targetWorld.getDimension().getType() == DimensionType.THE_NETHER) {
            InGameTimer.complete();
            return;
        }

        //Enter End
        if (timer.getCategory() == RunCategories.ENTER_END && targetWorld.getDimension().getType() == DimensionType.THE_END) {
            InGameTimer.complete();
        }

        RunCategories.checkAllBossesCompleted();
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


    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/toast/ToastManager;draw()V", shift = At.Shift.AFTER))
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
                && !(this.currentScreen instanceof TimerCustomizeScreen)) {
            SpeedRunIGTClient.TIMER_DRAWER.draw();
        }
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
                try {
                    HashMap<Identifier, List<Font>> map = new HashMap<>();

                    File[] fontFiles = SpeedRunIGT.FONT_PATH.toFile().listFiles();
                    if (fontFiles == null) return new HashMap<>();

                    for (File file : Arrays.stream(fontFiles).filter(file -> file.getName().endsWith(".ttf")).collect(Collectors.toList())) {
                        File config = SpeedRunIGT.FONT_PATH.resolve(file.getName().substring(0, file.getName().length() - 4) + ".json").toFile();
                        if (config.exists()) {
                            FontUtils.addFont(map, file, config);
                        } else {
                            FontUtils.addFont(map, file, null);
                        }
                    }
                    return map;
                } catch (Throwable e) {
                    return new HashMap<>();
                }
            }

            @Override
            protected void apply(Map<Identifier, List<Font>> loader, ResourceManager manager, Profiler profiler) {
                try {
                    for (Map.Entry<Identifier, List<Font>> listEntry : loader.entrySet()) {
                        FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftClientAccessor) MinecraftClient.getInstance()).getFontManager();
                        fontManager.getTextRenderers().computeIfAbsent(listEntry.getKey(),
                                        (identifierX) -> new TextRenderer(fontManager.getTextureManager(),
                                                new FontStorage(fontManager.getTextureManager(), identifierX)))
                                .setFonts(listEntry.getValue());
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
    @Inject(method = "addDetailsToCrashReport", at = @At("HEAD"))
    public void onCrash(CrashReport report, CallbackInfoReturnable<CrashReport> cir) {
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
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE && disconnectCheck) {
            InGameTimer.leave();
        }
    }
}
