package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.class_4112;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4112.class)
public abstract class MouseMixin {

    @Shadow public abstract boolean method_18252();

    @Inject(at = @At("HEAD"), method = "method_18241")
    public void onMove(CallbackInfo ci) {
        unlock();
    }

    @Inject(at = @At("HEAD"), method = "method_18246")
    public void onMouseScroll(CallbackInfo ci) {
        unlock();
    }

    private void unlock() {
        @NotNull
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() == TimerStatus.NONE || timer.getStatus() == TimerStatus.COMPLETED_LEGACY) return;

        if (timer.getStatus() == TimerStatus.IDLE && this.method_18252() && !MinecraftClient.getInstance().isPaused() && InGameTimer.checkingWorld) {
            timer.setPause(false);
        }
        if (this.method_18252() && !MinecraftClient.getInstance().isPaused()) {
            timer.updateFirstInput();
        }
    }
}
