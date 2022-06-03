package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerClientUtils;
import com.redlimerl.speedrunigt.utils.MixinValues;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void drawTimer(CallbackInfo ci) {
        MixinValues.IS_RENDERED_BEFORE = true;
    }

    @Inject(method="render",at=@At(value = "INVOKE", target = "Lnet/minecraft/client/MouseInput;updateMouse()V", shift = At.Shift.AFTER))
    public void getMoved(CallbackInfo ci){
        if(this.client.mouse.x != 0 || this.client.mouse.y != 0){
            unlock();
        }
    }

    private void unlock() {
        InGameTimer timer = InGameTimer.getInstance();
        if (InGameTimerClientUtils.canUnpauseTimer(false)) {
            timer.setPause(false, "moved mouse");
        }
        if (Display.isActive() && !MinecraftClient.getInstance().isPaused() && Mouse.isGrabbed()) {
            timer.updateFirstInput();
        }
    }
}
