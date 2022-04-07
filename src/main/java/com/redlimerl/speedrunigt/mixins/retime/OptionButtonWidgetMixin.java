package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.mixins.retime.accessor.OptionSliderWidgetAccessor;
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

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "buttonClicked", at = @At("RETURN"))
    public void onClickOption(ButtonWidget button, CallbackInfo ci) {
        if (((Object) this) instanceof OptionButtonWidget) {
            InGameTimerUtils.CHANGED_OPTIONS.add(((OptionButtonWidget) ((Object) this)).getOption());
        }
        if (((Object) this) instanceof OptionSliderWidget) {
            InGameTimerUtils.CHANGED_OPTIONS.add(((OptionSliderWidgetAccessor) this).getOption());
        }
    }
}
