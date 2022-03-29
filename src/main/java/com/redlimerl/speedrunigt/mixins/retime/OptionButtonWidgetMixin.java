package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;
import net.minecraft.client.gui.widget.OptionSliderWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class OptionButtonWidgetMixin {

    @Inject(method = "method_21930", at = @At("RETURN"))
    public void onClickOption(ButtonWidget button, CallbackInfo ci) {
        if (button instanceof OptionButtonWidget || button instanceof OptionSliderWidget) {
            InGameTimerUtils.RETIME_IS_CHANGED_OPTION = true;
        }
    }
}
