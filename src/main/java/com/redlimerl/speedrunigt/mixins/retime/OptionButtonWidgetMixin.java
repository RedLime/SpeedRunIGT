package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractPressableButtonWidget.class)
public class OptionButtonWidgetMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "onClick", at = @At("TAIL"))
    public void onClickOption(double mouseX, double mouseY, CallbackInfo ci) {
        if (((Object) this) instanceof OptionButtonWidget) {
            OptionButtonWidget optionButton = (OptionButtonWidget) ((Object) this);
            InGameTimerUtils.CHANGED_OPTIONS.add(optionButton.getOption());
        }
    }
}
