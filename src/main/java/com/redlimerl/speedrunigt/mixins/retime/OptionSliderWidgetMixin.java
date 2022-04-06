package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.widget.GameOptionSliderWidget;
import net.minecraft.client.options.DoubleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptionSliderWidget.class)
public class OptionSliderWidgetMixin {

    @Shadow @Final private DoubleOption option;

    @Inject(method = "applyValue", at = @At("TAIL"))
    public void onClickOption(CallbackInfo ci) {
        InGameTimerUtils.CHANGED_OPTIONS.add(this.option);
    }
}
