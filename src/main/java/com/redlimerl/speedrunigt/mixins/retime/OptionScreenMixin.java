package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionScreenMixin {

    @Inject(method = "method_0_2778", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelProperties;method_208(Lnet/minecraft/world/Difficulty;)V", shift = At.Shift.AFTER))
    public void onChangeDifficulty(ClickableWidget button, CallbackInfo ci) {
        SpeedRunIGT.debug("detected");
        InGameTimerUtils.CHANGED_OPTIONS.add(button);
    }

}
