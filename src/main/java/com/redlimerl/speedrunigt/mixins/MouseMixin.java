package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Shadow public abstract boolean isCursorLocked();

    @Inject(at = @At("HEAD"), method = "onCursorPos")
    public void onMove(CallbackInfo ci) {
        unlock();
    }

    @Inject(at = @At("HEAD"), method = "onMouseScroll")
    public void onMouseScroll(CallbackInfo ci) {
        unlock();
    }

    private void unlock() {
        @NotNull
        InGameTimer timer = InGameTimer.getInstance();
        if ((timer.getStatus() == TimerStatus.IDLE || timer.getStatus() == TimerStatus.LEAVE) && this.isCursorLocked() && !MinecraftClient.getInstance().isPaused() && InGameTimer.checkingWorld) {
            timer.setPause(false);
        }
        if (this.isCursorLocked() && !MinecraftClient.getInstance().isPaused()) {
            timer.updateFirstInput();
        }
    }
}
