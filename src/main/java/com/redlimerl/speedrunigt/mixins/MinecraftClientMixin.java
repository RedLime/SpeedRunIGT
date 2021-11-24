package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Objects;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public abstract boolean isInSingleplayer();

    @Shadow public abstract boolean isPaused();

    @Shadow @Nullable public Screen currentScreen;
    @Shadow @Final public GameOptions options;
    private static String lastWorldName = null;
    private static boolean lastWorldOpen = false;

    private @NotNull
    final InGameTimer timer = InGameTimer.INSTANCE;

    @Inject(at = @At("HEAD"), method = "createWorld")
    public void onCreate(String worldName, LevelInfo levelInfo, DynamicRegistryManager.Impl registryTracker, GeneratorOptions generatorOptions, CallbackInfo ci) {
        if (timer.getStatus() != TimerStatus.NONE) {
            timer.end();
        }
        lastWorldName = worldName;
        lastWorldOpen = true;
    }

    @Inject(at = @At("HEAD"), method = "startIntegratedServer(Ljava/lang/String;)V")
    public void onWorldOpen(String worldName, CallbackInfo ci) {
        lastWorldOpen = worldName.equals(lastWorldName);
        if (!lastWorldOpen) {
            timer.end();
        }
    }

    @Inject(at = @At("HEAD"), method = "joinWorld")
    public void onJoin(ClientWorld world, CallbackInfo ci) {
        if (!isInSingleplayer()) return;

        if (this.timer.getStatus() == TimerStatus.NONE && lastWorldOpen) {
            this.timer.start();
        } else if (lastWorldOpen) {
            this.timer.setPause(true, TimerStatus.IDLE);
        }

        //Enter Nether
        if (timer.getCategory() == RunCategory.ENTER_NETHER && Objects.equals(world.getRegistryKey().getValue().toString(), DimensionType.THE_NETHER_ID.toString())) {
            timer.complete();
        }

        //Enter End
        if (timer.getCategory() == RunCategory.ENTER_END && Objects.equals(world.getRegistryKey().getValue().toString(), DimensionType.THE_END_ID.toString())) {
            timer.complete();
        }
    }

    @Inject(at = @At("HEAD"), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
    public void onDisconnect(Screen screen, CallbackInfo ci) {
        if (this.timer.getStatus() != TimerStatus.NONE && screen instanceof SaveLevelScreen) {
            if (this.timer.getStatus() == TimerStatus.COMPLETED) {
                this.timer.end();
            } else {
                this.timer.setPause(true, TimerStatus.LEAVE);
            }
        }
    }

    @ModifyVariable(method = "render(Z)V", at = @At(value = "STORE"), ordinal = 1)
    private boolean renderMixin(boolean paused) {
        if (this.timer.getStatus() == TimerStatus.RUNNING && paused) {
            this.timer.setPause(true, TimerStatus.PAUSED);
        } else if (this.timer.getStatus() == TimerStatus.PAUSED && !paused) {
            this.timer.setPause(false);
        }

        return paused;
    }

    @Inject(method = "handleInputEvents", at = @At(value = "HEAD"))
    private void slotChange(CallbackInfo ci) {
        GameOptions o = this.options;
        if (timer.getStatus() == TimerStatus.IDLE) {
            if (o.keyAttack.isPressed() || o.keyDrop.isPressed() || o.keyInventory.isPressed() || o.keySneak.wasPressed() || o.keySwapHands.isPressed()
                    || o.keyUse.isPressed() || o.keyPickItem.isPressed() || o.keySprint.wasPressed() || Arrays.stream(o.keysHotbar).anyMatch(KeyBinding::isPressed)) {
                timer.setPause(false);
            }
        }
    }


    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/util/math/MatrixStack;)V", shift = At.Shift.AFTER))
    private void drawTimer(CallbackInfo ci) {
        if (!this.options.hudHidden && this.isInSingleplayer() && timer.getStatus() != TimerStatus.NONE
                && (!this.isPaused() || this.currentScreen instanceof GameMenuScreen) && !(this.currentScreen instanceof ChatScreen)) {
            SpeedRunIGT.TIMER_DRAWER.draw();
        }
    }
}
