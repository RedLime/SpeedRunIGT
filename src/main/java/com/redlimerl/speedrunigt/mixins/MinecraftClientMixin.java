package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.instance.GameInstance;
import com.redlimerl.speedrunigt.mixins.access.FontManagerAccessor;
import com.redlimerl.speedrunigt.mixins.access.MinecraftClientAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.timer.*;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.utils.FontUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontFilterType;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow @Final public GameOptions options;

    @Shadow @Nullable public ClientWorld world;

    @Shadow @Final private ReloadableResourceManagerImpl resourceManager;

    @Shadow private boolean paused;

    @Inject(method = "setScreen", at = @At("RETURN"))
    public void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof LevelLoadingScreen) {
            InGameTimerUtils.CAN_DISCONNECT = true;
        }
        if (InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN != null) {
            Screen screen1 = InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN;
            InGameTimerClientUtils.FAILED_CATEGORY_INIT_SCREEN = null;
            MinecraftClient.getInstance().setScreen(screen1);
        }
    }

    @Inject(at = @At("HEAD"), method = "joinWorld")
    public void onJoin(ClientWorld targetWorld, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci) {
        if (targetWorld == null) return;

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() == TimerStatus.NONE) return;

        InGameTimerUtils.IS_CHANGING_DIMENSION = false;
        timer.setPause(true, TimerStatus.IDLE, "changed dimension");

        // For Timelines
        if (Objects.equals(targetWorld.getRegistryKey().getValue().toString(), DimensionTypes.THE_NETHER_ID.toString())) {
            timer.tryInsertNewTimeline("enter_nether");
        } else if (Objects.equals(targetWorld.getRegistryKey().getValue().toString(), DimensionTypes.THE_END_ID.toString())) {
            timer.tryInsertNewTimeline("enter_end");
        }

        //Enter Nether
        if (timer.getCategory() == RunCategories.ENTER_NETHER && Objects.equals(targetWorld.getRegistryKey().getValue().toString(), DimensionTypes.THE_NETHER_ID.toString())) {
            InGameTimer.complete();
            return;
        }

        //Enter End
        if (timer.getCategory() == RunCategories.ENTER_END && Objects.equals(targetWorld.getRegistryKey().getValue().toString(), DimensionTypes.THE_END_ID.toString())) {
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


    /**
     * Add import font system
     */
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/debug/DebugRenderer;<init>(Lnet/minecraft/client/MinecraftClient;)V", shift = At.Shift.BEFORE))
    public void onInit(RunArgs args, CallbackInfo ci) {
        this.resourceManager.registerReloader(new SinglePreparationResourceReloader<Map<Identifier, List<Font>>>() {
            @Override
            protected Map<Identifier, List<Font>> prepare(ResourceManager manager, Profiler profiler) {
                SpeedRunIGT.FONT_MAPS.clear();

                HashMap<Identifier, List<Font>> map = new HashMap<>();

                File[] fontFiles = SpeedRunIGT.FONT_PATH.toFile().listFiles();
                if (fontFiles == null) return new HashMap<>();

                for (File file : Arrays.stream(fontFiles).filter(file -> file.getName().endsWith(".ttf") || file.getName().endsWith(".otf")).toList()) {
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
                    EnumSet<FontFilterType> set = EnumSet.noneOf(FontFilterType.class);
                    if (options.getForceUnicodeFont().getValue()) {
                        set.add(FontFilterType.UNIFORM);
                    }
                    if (options.getJapaneseGlyphVariants().getValue()) {
                        set.add(FontFilterType.JAPANESE_VARIANTS);
                    }
                    FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftClientAccessor) MinecraftClient.getInstance()).getFontManager();
                    for (Map.Entry<Identifier, List<Font>> listEntry : loader.entrySet()) {
                        FontStorage fontStorage = new FontStorage(fontManager.getTextureManager(), listEntry.getKey());
                        fontStorage.setFonts(listEntry.getValue().stream().map(font -> new Font.FontFilterPair(font, FontFilterType.FilterMap.NO_FILTER)).collect(Collectors.toList()), set);
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
    private void onCrash(CallbackInfo ci) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) InGameTimer.leave();
    }

    // Crash safety
    @Inject(method = "printCrashReport(Lnet/minecraft/client/MinecraftClient;Ljava/io/File;Lnet/minecraft/util/crash/CrashReport;)V", at = @At("HEAD"))
    private static void onCrash(MinecraftClient client, File runDirectory, CrashReport crashReport, CallbackInfo ci) {
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
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE && InGameTimerUtils.CAN_DISCONNECT) {
            GameInstance.getInstance().callEvents("leave_world");
            InGameTimer.leave();
        }
    }
}