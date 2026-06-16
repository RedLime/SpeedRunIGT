package com.redlimerl.speedrunigt.mixins;

import com.mojang.blaze3d.font.GlyphProvider;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.instance.GameInstance;
import com.redlimerl.speedrunigt.mixins.access.FontManagerAccessor;
import com.redlimerl.speedrunigt.mixins.access.MinecraftAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.timer.*;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.utils.FontUtils;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow @Final public Options options;

    @Shadow @Nullable public ClientLevel level;

    @Shadow @Final private ReloadableResourceManager resourceManager;

    @Shadow private boolean pause;

    @Inject(at = @At("HEAD"), method = "setLevel")
    public void onJoin(ClientLevel world, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() == TimerStatus.NONE) return;

        InGameTimerUtils.IS_CHANGING_DIMENSION = false;
        timer.setPause(true, TimerStatus.IDLE, "changed dimension");

        // For Timelines
        if (Objects.equals(world.dimension().identifier().toString(), BuiltinDimensionTypes.NETHER.toString())) {
            timer.tryInsertNewTimeline("enter_nether");
        } else if (Objects.equals(world.dimension().identifier().toString(), BuiltinDimensionTypes.END.toString())) {
            timer.tryInsertNewTimeline("enter_end");
        }

        //Enter Nether
        if (timer.getCategory() == RunCategories.ENTER_NETHER && Objects.equals(world.dimension().identifier().toString(), BuiltinDimensionTypes.NETHER.toString())) {
            InGameTimer.complete();
            return;
        }

        //Enter End
        if (timer.getCategory() == RunCategories.ENTER_END && Objects.equals(world.dimension().identifier().toString(), BuiltinDimensionTypes.END.toString())) {
            InGameTimer.complete();
        }

        RunCategories.checkAllBossesCompleted();
    }

    @Unique
    private int saveTickCount = 0;
    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickMixin(CallbackInfo ci) {
        if (++this.saveTickCount >= 20) {
            SpeedRunOption.checkSave();
            this.saveTickCount = 0;
        }
    }

    @Inject(method = "runTick(Z)V", at = @At("TAIL"))
    private void renderMixin(boolean tick, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.RUNNING && this.pause) {
            timer.setPause(true, TimerStatus.PAUSED, "player");
            if (InGameTimerClientUtils.getGeneratedChunkRatio() < 0.1f) {
                InGameTimerUtils.RETIME_IS_WAITING_LOAD = true;
            }
        } else if (timer.getStatus() == TimerStatus.PAUSED && !this.pause) {
            timer.setPause(false, "player");
        }
    }


    /**
     * Add import font system
     */
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;updateFontOptions()V"))
    public void onInit(GameConfig args, CallbackInfo ci) {
        this.resourceManager.registerReloadListener(new SimplePreparableReloadListener<Map<Identifier, List<GlyphProvider>>>() {
            @Override
            protected Map<Identifier, List<GlyphProvider>> prepare(ResourceManager manager, ProfilerFiller profiler) {
                SpeedRunIGT.FONT_MAPS.clear();

                HashMap<Identifier, List<GlyphProvider>> map = new HashMap<>();

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
            protected void apply(Map<Identifier, List<GlyphProvider>> loader, ResourceManager manager, ProfilerFiller profiler) {
                try {
                    EnumSet<FontOption> set = EnumSet.noneOf(FontOption.class);
                    if (options.forceUnicodeFont().get()) {
                        set.add(FontOption.UNIFORM);
                    }
                    if (options.japaneseGlyphVariants().get()) {
                        set.add(FontOption.JAPANESE_VARIANTS);
                    }
                    FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftAccessor) Minecraft.getInstance()).getFontManager();
                    for (Map.Entry<Identifier, List<GlyphProvider>> listEntry : loader.entrySet()) {
                        GlyphStitcher glyphBaker = new GlyphStitcher(fontManager.getTextureManager(), listEntry.getKey());
                        FontSet fontStorage = new FontSet(glyphBaker);
                        fontStorage.reload(listEntry.getValue().stream().map(font -> new GlyphProvider.Conditional(font, FontOption.Filter.ALWAYS_PASS)).collect(Collectors.toList()), set);
                        fontManager.getFontSets().put(listEntry.getKey(), fontStorage);
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
    @Inject(method = "emergencySave", at = @At("HEAD"))
    private void onCrash(CallbackInfo ci) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) InGameTimer.leave();
    }

    // Crash safety
    @Inject(method = "saveReport(Ljava/io/File;Lnet/minecraft/CrashReport;)V", at = @At("HEAD"))
    private static void onCrash(File runDirectory, CrashReport crashReport, CallbackInfo ci) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) InGameTimer.leave();
    }

    // Record save
    @Inject(method = "exitWorldAndClose", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;close()V"))
    public void onStop(CallbackInfo ci) {
        InGameTimer.getInstance().writeRecordFile(false);
    }

// Disconnecting fix
    @Inject(at = @At("HEAD"), method = "disconnect*")
    public void disconnect(CallbackInfo ci) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE && InGameTimerUtils.CAN_DISCONNECT) {
            GameInstance.getInstance().callEvents("leave_world");
            InGameTimer.leave();
        }
    }
}