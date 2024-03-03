package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screen.SettingsScreen$6")
public class OptionScreenMixin {

    @Inject(method = "method_18374", remap = false, at = @At("TAIL"))
    public void onChangeDifficulty(double d, double e, CallbackInfo ci) {
        SpeedRunIGT.debug("detected");
        InGameTimerUtils.CHANGED_OPTIONS.add(this);
    }

}
