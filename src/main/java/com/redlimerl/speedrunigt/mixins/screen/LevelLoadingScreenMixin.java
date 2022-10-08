package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public class LevelLoadingScreenMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        InGameTimerUtils.LATEST_TIMER_TIME = System.currentTimeMillis();
    }
}
