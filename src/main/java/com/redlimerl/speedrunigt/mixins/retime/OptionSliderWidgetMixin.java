package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.mixins.access.DoubleOptionSliderWidgetAccessor;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.widget.DoubleOptionSliderWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SliderWidget.class)
public class OptionSliderWidgetMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "setValueFromMouse", at = @At("TAIL"))
    public void onClickOption(double mouseX, CallbackInfo ci) {
        if (((Object) this) instanceof DoubleOptionSliderWidget) {
            DoubleOptionSliderWidgetAccessor optionSlider = (DoubleOptionSliderWidgetAccessor) this;
            InGameTimerUtils.CHANGED_OPTIONS.add(optionSlider.getOption());
        }
    }
}
