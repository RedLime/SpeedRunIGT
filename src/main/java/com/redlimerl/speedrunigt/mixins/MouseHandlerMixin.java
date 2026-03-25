package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerClientUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow public abstract boolean isMouseGrabbed();

    @Inject(at = @At("HEAD"), method = "onMove")
    public void onMove(CallbackInfo ci) {
        this.unlock();
    }

    @Inject(at = @At("HEAD"), method = "onScroll")
    public void onMouseScroll(CallbackInfo ci) {
        this.unlock();
    }

    private void unlock() {
        @NotNull
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() == TimerStatus.NONE || timer.getStatus() == TimerStatus.COMPLETED_LEGACY) return;

        if (InGameTimerClientUtils.canUnpauseTimer(false)) {
            timer.setPause(false, "moved mouse");
        }
        if (this.isMouseGrabbed() && !Minecraft.getInstance().isPaused()) {
            timer.updateFirstInput();
        }
    }
}
