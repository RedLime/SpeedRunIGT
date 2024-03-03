package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SettingsScreen.class)
public class OptionScreenMixin {
    @Inject(method = "method_19833", remap = false, at = @At("TAIL"))
    public void onChangeDifficulty(ButtonWidget buttonWidget, CallbackInfo ci) {
        InGameTimerUtils.CHANGED_OPTIONS.add(buttonWidget);
    }
}
