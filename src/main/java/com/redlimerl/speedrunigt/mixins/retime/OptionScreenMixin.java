package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SettingsScreen.class)
public class OptionScreenMixin {

    @Inject(method = "buttonClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;setDifficulty(Lnet/minecraft/world/Difficulty;)V", shift = At.Shift.AFTER))
    public void onChangeDifficulty(ButtonWidget button, CallbackInfo ci) {
        SpeedRunIGT.debug("detected");
        InGameTimerUtils.CHANGED_OPTIONS.add(button);
    }

}
