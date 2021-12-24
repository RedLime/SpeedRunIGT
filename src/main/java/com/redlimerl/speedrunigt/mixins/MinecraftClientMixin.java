package com.redlimerl.speedrunigt.mixins;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.TextureUtil;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.font.*;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelInfo;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public abstract boolean isInSingleplayer();

    @Shadow public GameOptions options;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Shadow public abstract boolean isPaused();

    @Shadow @Nullable public Screen currentScreen;

    @Shadow public WorldRenderer worldRenderer;

    @Shadow @Nullable public ClientWorld world;

    @Shadow public abstract boolean isWindowFocused();

    @Shadow private ReloadableResourceManager resourceManager;

    @Shadow public abstract Profiler getProfiler();

    @Shadow public Mouse mouse;

    @Inject(at = @At("HEAD"), method = "startIntegratedServer")
    public void onCreate(String name, String displayName, LevelInfo levelInfo, CallbackInfo ci) {
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
    }

    private static DimensionType currentDimension = null;

    @Inject(at = @At("HEAD"), method = "joinWorld")
    public void onJoin(ClientWorld targetWorld, CallbackInfo ci) {
        if (!isInSingleplayer()) return;
        InGameTimer timer = InGameTimer.getInstance();

        currentDimension = targetWorld.getDimension().getType();
        InGameTimer.checkingWorld = true;

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.setPause(true, TimerStatus.IDLE);
        }

        //Enter Nether
        if (timer.getCategory() == RunCategory.ENTER_NETHER && targetWorld.getDimension().getType() == DimensionType.THE_NETHER) {
            InGameTimer.complete();
        }

        //Enter End
        if (timer.getCategory() == RunCategory.ENTER_END && targetWorld.getDimension().getType() == DimensionType.THE_END) {
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
            target = "Lnet/minecraft/client/toast/ToastManager;draw()V", shift = At.Shift.AFTER))
    private void drawTimer(CallbackInfo ci) {
        this.getProfiler().swap("timer");
        InGameTimer timer = InGameTimer.getInstance();

        if (worldRenderer != null && world != null && world.getDimension().getType() == currentDimension && !isPaused() && isWindowFocused()
                && timer.getStatus() == TimerStatus.IDLE && InGameTimer.checkingWorld && this.mouse.isCursorLocked()) {
            int chunks = worldRenderer.getCompletedChunkCount();
            int entities = worldRenderer.regularEntityCount - (options.perspective > 0 ? 0 : 1);

            if (chunks + entities > 0) {
                if (!(SpeedRunOptions.getOption(SpeedRunOptions.WAITING_FIRST_INPUT) && !timer.isStarted())) {
                    System.out.println("aaaaaaaa");
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
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/debug/DebugRenderer;<init>(Lnet/minecraft/client/MinecraftClient;)V", shift = At.Shift.BEFORE))
    public void onInit(CallbackInfo ci) {
        this.resourceManager.registerListener(new SinglePreparationResourceReloadListener<Map<Identifier, List<Font>>>() {
            @Override
            protected Map<Identifier, List<Font>> prepare(ResourceManager manager, Profiler profiler) {
                HashMap<Identifier, List<Font>> map = new HashMap<>();

                File[] fontFiles = SpeedRunIGT.FONT_PATH.toFile().listFiles();
                if (fontFiles == null) return new HashMap<>();

                for (File file : Arrays.stream(fontFiles).filter(file -> file.getName().endsWith(".ttf")).collect(Collectors.toList())) {
                    File config = SpeedRunIGT.FONT_PATH.resolve(file.getName().substring(0, file.getName().length() - 4) + ".json").toFile();
                    if (config.exists()) {
                        addFont(map, file, config);
                    } else {
                        addFont(map, file, null);
                    }
                }
                return map;
            }

            @Override
            protected void apply(Map<Identifier, List<Font>> loader, ResourceManager manager, Profiler profiler) {
                for (Map.Entry<Identifier, List<Font>> listEntry : loader.entrySet()) {
                    MinecraftClient.getInstance().fontManager.textRenderers.computeIfAbsent(listEntry.getKey(),
                                    (identifierX) -> new TextRenderer(MinecraftClient.getInstance().fontManager.textureManager, new FontStorage(MinecraftClient.getInstance().fontManager.textureManager, identifierX)))
                            .setFonts(listEntry.getValue());
                }
            }
        });
    }

    private static void addFont(HashMap<Identifier, List<Font>> map, File file, File configFile) {
        FileInputStream fileInputStream = null;
        ByteBuffer byteBuffer = null;
        Throwable throwable = null;

        try {
            fileInputStream = new FileInputStream(file);
            byteBuffer = TextureUtil.readResource(fileInputStream);
            byteBuffer.flip();

            Identifier fontIdentifier = new Identifier(SpeedRunIGT.MOD_ID, file.getName().toLowerCase(Locale.ROOT).replace(".ttf", "").replaceAll(" ", "_").replaceAll("[^a-z0-9/._-]", ""));
            ArrayList<Font> fontArrayList = new ArrayList<>();

            if (configFile != null && configFile.exists()) {
                JsonObject configure = new JsonParser().parse(FileUtils.readFileToString(configFile, StandardCharsets.UTF_8)).getAsJsonObject();
                fontArrayList.add(new TrueTypeFont(TrueTypeFont.getSTBTTFontInfo(byteBuffer),
                        configure.has("size") ? configure.get("size").getAsFloat() : 11f,
                        configure.has("oversample") ? configure.get("oversample").getAsFloat() : 6f,
                        configure.has("shift") && configure.get("shift").isJsonArray() && configure.get("shift").getAsJsonArray().size() >= 1 ? configure.get("shift").getAsJsonArray().get(0).getAsFloat() : 0f,
                        configure.has("shift") && configure.get("shift").isJsonArray() && configure.get("shift").getAsJsonArray().size() >= 2 ? configure.get("shift").getAsJsonArray().get(1).getAsFloat() : 0f,
                        configure.has("skip") ? configure.get("skip").getAsString() : ""));
            } else {
                fontArrayList.add(new TrueTypeFont(TrueTypeFont.getSTBTTFontInfo(byteBuffer), 11f, 6f, 0, 0, ""));
            }

            fontArrayList.add(new BlankFont());

            map.put(fontIdentifier, fontArrayList);
        } catch (FileNotFoundException e) {
            MemoryUtil.memFree(byteBuffer);
        } catch (IOException throwable1) {
            throwable = throwable1;
        } finally {
            try {
                if (fileInputStream != null) fileInputStream.close();
            } catch (IOException e) {
                if (throwable != null) throwable.addSuppressed(e);
            }
        }
    }
}
