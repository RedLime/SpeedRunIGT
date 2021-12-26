package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.utils.FontUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public abstract boolean isInSingleplayer();

    @Shadow @Final public GameOptions options;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Shadow public abstract boolean isPaused();

    @Shadow @Nullable public Screen currentScreen;

    @Shadow @Final public WorldRenderer worldRenderer;

    @Shadow @Nullable public ClientWorld world;

    @Shadow public abstract boolean isWindowFocused();

    @Shadow @Final private ReloadableResourceManager resourceManager;

    @Shadow private Profiler profiler;

    @Inject(at = @At("HEAD"), method = "createWorld")
    public void onCreate(String worldName, LevelInfo levelInfo, DynamicRegistryManager.Impl registryTracker, GeneratorOptions generatorOptions, CallbackInfo ci) {
        InGameTimer.start();
        currentDimension = null;
        InGameTimer.currentWorldName = worldName;
    }

    @Inject(at = @At("HEAD"), method = "startIntegratedServer(Ljava/lang/String;)V")
    public void onWorldOpen(String worldName, CallbackInfo ci) {
        try {
            boolean loaded = InGameTimer.load(worldName);
            if (!loaded) InGameTimer.end();
            else {
                InGameTimer.currentWorldName = worldName;
            }
            currentDimension = null;
        } catch (Exception e) {
            InGameTimer.end();
            currentDimension = null;
            SpeedRunIGT.debug("Exception in timer load, can't load the timer.");
            e.printStackTrace();
        }
    }

    private static DimensionType currentDimension = null;

    @Inject(at = @At("HEAD"), method = "joinWorld")
    public void onJoin(ClientWorld targetWorld, CallbackInfo ci) {
        if (!isInSingleplayer()) return;
        InGameTimer timer = InGameTimer.getInstance();

        currentDimension = targetWorld.getDimension();
        InGameTimer.checkingWorld = true;

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.setPause(true, TimerStatus.IDLE);
        }

        //Enter Nether
        if (timer.getCategory() == RunCategory.ENTER_NETHER && Objects.equals(targetWorld.getRegistryKey().getValue().toString(), DimensionType.THE_NETHER_ID.toString())) {
            InGameTimer.complete();
        }

        //Enter End
        if (timer.getCategory() == RunCategory.ENTER_END && Objects.equals(targetWorld.getRegistryKey().getValue().toString(), DimensionType.THE_END_ID.toString())) {
            InGameTimer.complete();
        }
    }

    @ModifyVariable(method = "render(Z)V", at = @At(value = "STORE"), ordinal = 1)
    private boolean renderMixin(boolean paused) {
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.RUNNING && paused) {
            timer.setPause(true, TimerStatus.PAUSED);
        } else if (timer.getStatus() == TimerStatus.PAUSED && !paused) {
            timer.setPause(false);
        }

        return paused;
    }


    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/util/math/MatrixStack;)V", shift = At.Shift.AFTER))
    private void drawTimer(CallbackInfo ci) {
        this.profiler.swap("timer");
        InGameTimer timer = InGameTimer.getInstance();

        if (worldRenderer != null && world != null && world.getDimension() == currentDimension && !isPaused() && isWindowFocused()
                && timer.getStatus() == TimerStatus.IDLE && InGameTimer.checkingWorld) {
            int chunks = worldRenderer.getCompletedChunkCount();
            int entities = worldRenderer.regularEntityCount - (options.getPerspective().isFirstPerson() ? 0 : 1);

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
            SpeedRunIGT.TIMER_DRAWER.draw();
        }
    }


    /**
     * Add import font system
     */
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/debug/DebugRenderer;<init>(Lnet/minecraft/client/MinecraftClient;)V", shift = At.Shift.BEFORE))
    public void onInit(RunArgs args, CallbackInfo ci) {
        this.resourceManager.registerReloader(new SinglePreparationResourceReloader<Map<Identifier, List<Font>>>() {
            @Override
            protected Map<Identifier, List<Font>> prepare(ResourceManager manager, Profiler profiler) {
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
                    return Map.of();
                }
            }

            @Override
            protected void apply(Map<Identifier, List<Font>> loader, ResourceManager manager, Profiler profiler) {
                try {
                    for (Map.Entry<Identifier, List<Font>> listEntry : loader.entrySet()) {
                        FontStorage fontStorage = new FontStorage(MinecraftClient.getInstance().fontManager.textureManager, listEntry.getKey());
                        fontStorage.setFonts(listEntry.getValue());
                        MinecraftClient.getInstance().fontManager.fontStorages.put(listEntry.getKey(), fontStorage);
                    }
                    TimerDrawer.fontHeightMap.clear();
                } catch (Throwable e) {
                    SpeedRunIGT.debug("Error! failed import timer fonts!");
                    e.printStackTrace();
                }
            }
        });
    }
}
