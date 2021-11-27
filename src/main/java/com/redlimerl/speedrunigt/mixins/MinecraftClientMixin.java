package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public abstract boolean isInSingleplayer();

    @Shadow @Final public GameOptions options;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Shadow public abstract boolean isPaused();

    @Shadow @Nullable public Screen currentScreen;

    @Shadow @Final public WorldRenderer worldRenderer;

    @Shadow @Nullable public ClientWorld world;


    @Inject(at = @At("HEAD"), method = "method_29607(Ljava/lang/String;Lnet/minecraft/world/level/LevelInfo;Lnet/minecraft/util/registry/RegistryTracker$Modifiable;Lnet/minecraft/world/gen/GeneratorOptions;)V")
    public void onCreate(String worldName, LevelInfo levelInfo, RegistryTracker.Modifiable registryTracker, GeneratorOptions generatorOptions, CallbackInfo ci) {
        InGameTimer.start();
        InGameTimer.currentWorldName = worldName;
    }

    @Inject(at = @At("HEAD"), method = "startIntegratedServer(Ljava/lang/String;)V")
    public void onWorldOpen(String worldName, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        boolean loaded = InGameTimer.load(worldName);
        if (!loaded) timer.end();
        else {
            InGameTimer.currentWorldName = worldName;
            timer.setPause(true, TimerStatus.IDLE);
        }
    }

    private static ClientWorld currWorld;
    @Inject(at = @At("HEAD"), method = "joinWorld")
    public void onJoin(ClientWorld targetWorld, CallbackInfo ci) {
        if (!isInSingleplayer()) return;
        InGameTimer timer = InGameTimer.getInstance();

        currWorld = targetWorld;

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.setPause(true, TimerStatus.IDLE);
            InGameTimer.renderedWorld = 0;
        }

        //Enter Nether
        if (timer.getCategory() == RunCategory.ENTER_NETHER && targetWorld.getDimensionRegistryKey() == DimensionType.THE_NETHER_REGISTRY_KEY) {
            timer.complete();
        }

        //Enter End
        if (timer.getCategory() == RunCategory.ENTER_END && targetWorld.getDimensionRegistryKey() == DimensionType.THE_END_REGISTRY_KEY) {
            timer.complete();
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

    @Inject(method = "handleInputEvents", at = @At(value = "HEAD"))
    private void slotChange(CallbackInfo ci) {
        GameOptions o = this.options;
        InGameTimer timer = InGameTimer.getInstance();

        if (o.keyAttack.isPressed() || o.keyDrop.isPressed() || o.keyInventory.isPressed() || o.keySneak.wasPressed() || o.keySwapHands.isPressed()
                || o.keyUse.isPressed() || o.keyPickItem.isPressed() || o.keySprint.wasPressed() || Arrays.stream(o.keysHotbar).anyMatch(KeyBinding::isPressed)) {
            if (timer.getStatus() == TimerStatus.IDLE) {
                timer.setPause(false);
            }
            timer.updateFirstInput();
        }
    }


    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/util/math/MatrixStack;)V", shift = At.Shift.AFTER))
    private void drawTimer(CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        if ((timer.getStatus() == TimerStatus.IDLE || (timer.getStatus() == TimerStatus.PAUSED && timer.getRawTicks() == 0))
                && !isPaused() && world == currWorld && (!SpeedRunOptions.getOption(SpeedRunOptions.WAITING_FIRST_INPUT) || timer.isStarted())) {
            int chunks = worldRenderer.getCompletedChunkCount();
            int entities = worldRenderer.regularEntityCount - (options.perspective > 0 ? 0 : 1);

            if (chunks + entities > 0) {
                timer.setPause(false);
            }
        }
        SpeedRunIGT.DEBUG_DATA = timer.getStatus().name();
        if (!this.options.hudHidden && this.isInSingleplayer() && this.world != null && timer.getStatus() != TimerStatus.NONE
                && (!this.isPaused() || this.currentScreen instanceof GameMenuScreen) && !(this.currentScreen instanceof ChatScreen)) {
            SpeedRunIGT.TIMER_DRAWER.draw();
        }
    }
}
