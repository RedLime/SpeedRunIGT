package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.config.Options;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.Inventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
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

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public abstract boolean isInSingleplayer();

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final public GameOptions options;
    @Shadow @Final public TextRenderer textRenderer;

    @Shadow public abstract boolean isPaused();

    @Shadow @Nullable public Screen currentScreen;
    @Shadow @Final private Window window;
    private static String lastWorldName = null;
    private static boolean lastWorldOpen = false;

    private @NotNull
    final InGameTimer timer = InGameTimer.INSTANCE;

    @Inject(at = @At("HEAD"), method = "method_29607(Ljava/lang/String;Lnet/minecraft/world/level/LevelInfo;Lnet/minecraft/util/registry/RegistryTracker$Modifiable;Lnet/minecraft/world/gen/GeneratorOptions;)V")
    public void onCreate(String worldName, LevelInfo levelInfo, RegistryTracker.Modifiable registryTracker, GeneratorOptions generatorOptions, CallbackInfo ci) {
        LOGGER.log(Level.INFO, "world create");

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.end();
        }
        lastWorldName = worldName;
        lastWorldOpen = true;
    }

    @Inject(at = @At("HEAD"), method = "startIntegratedServer(Ljava/lang/String;)V")
    public void onWorldOpen(String worldName, CallbackInfo ci) {
        LOGGER.log(Level.INFO, "world open" + lastWorldOpen);
        lastWorldOpen = worldName.equals(lastWorldName);
    }

    @Inject(at = @At("HEAD"), method = "joinWorld")
    public void onJoin(ClientWorld world, CallbackInfo ci) {
        if (!isInSingleplayer()) return;
        LOGGER.log(Level.INFO, "world join");

        if (this.timer.getStatus() == TimerStatus.NONE && lastWorldOpen) {
            this.timer.start();
        } else if (lastWorldOpen) {
            this.timer.setPause(true, TimerStatus.IDLE);
        }
    }

    @Inject(at = @At("HEAD"), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
    public void onDisconnect(Screen screen, CallbackInfo ci) {
        LOGGER.log(Level.INFO, "world disconnect");

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
        if (!this.options.hudHidden && this.isInSingleplayer()) {
            MatrixStack matrixStack = new MatrixStack();
            Options.TimerPosition pos = Options.INSTANCE.getTimerPos();
            if (pos != Options.TimerPosition.NONE && timer.getStatus() != TimerStatus.NONE) {
                int x = 12, y = window.getScaledHeight() - 32;
                MutableText igt = new LiteralText("IGT: ").append(new LiteralText(InGameTimer.timeToStringFormat(timer.getInGameTime())));
                MutableText rta = new LiteralText("RTA: ").append(new LiteralText(InGameTimer.timeToStringFormat(timer.getRealTimeAttack())));
                switch (pos) {
                    case LEFT_TOP:
                        y = 12;
                        break;
                    case RIGHT_BOTTOM:
                        x = window.getScaledWidth() - 12 - this.textRenderer.getWidth(rta);
                        y = window.getScaledHeight() - 32;
                        break;
                    case RIGHT_TOP:
                        x = window.getScaledWidth() - 12 - this.textRenderer.getWidth(rta);
                        y = 12;
                        break;
                }
                if ((!this.isPaused() || this.currentScreen instanceof GameMenuScreen) && !(this.currentScreen instanceof ChatScreen)) {
                    drawOutLine(this.textRenderer, matrixStack, x, y+10, rta, Formatting.AQUA);
                    drawOutLine(this.textRenderer, matrixStack, x, y, igt, Formatting.YELLOW);
                    //drawOutLine(this.textRenderer, matrixStack, x, y-10, new LiteralText(timer.getStatus().name()), Formatting.RED);
                }
            }
        }
    }

    private static void drawOutLine(TextRenderer textRenderer, MatrixStack matrixStack, int x, int y, MutableText text, Formatting color) {
        textRenderer.draw(matrixStack, text, (float)x + 1, (float)y + 1, 0);
        textRenderer.draw(matrixStack, text, (float)x + 1, (float)y, 0);
        textRenderer.draw(matrixStack, text, (float)x + 1, (float)y - 1, 0);
        textRenderer.draw(matrixStack, text, (float)x, (float)y - 1, 0);
        textRenderer.draw(matrixStack, text, (float)x, (float)y + 1, 0);
        textRenderer.draw(matrixStack, text, (float)x - 1, (float)y + 1, 0);
        textRenderer.draw(matrixStack, text, (float)x - 1, (float)y, 0);
        textRenderer.draw(matrixStack, text, (float)x - 1, (float)y - 1, 0);
        textRenderer.draw(matrixStack, text.formatted(color), (float)x, (float)y, 16777215);
    }
}
