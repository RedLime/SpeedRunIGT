package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionScreenMixin {
    @Inject(method = "method_19833", remap = false, at = @At("TAIL"))
    public void onChangeDifficulty(ButtonWidget buttonWidget, CallbackInfo ci) {
        SpeedRunIGT.debug("detected");
        InGameTimerUtils.CHANGED_OPTIONS.add(buttonWidget);
    }
}
