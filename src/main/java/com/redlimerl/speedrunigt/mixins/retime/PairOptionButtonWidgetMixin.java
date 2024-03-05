package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screen.VideoOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoOptionsScreen.class)
public class PairOptionButtonWidgetMixin {
    @Inject(method = "buttonClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;setOption(Lnet/minecraft/client/option/GameOption;I)V", shift = At.Shift.AFTER))
    public void onClickOption(ButtonWidget button, CallbackInfo ci) {
        InGameTimerUtils.CHANGED_OPTIONS.add(((OptionButtonWidget) button).method_1088());
    }
}
