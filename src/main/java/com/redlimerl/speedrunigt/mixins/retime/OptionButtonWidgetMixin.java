package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(CyclingButtonWidget.class)
public class OptionButtonWidgetMixin {

    @Shadow @Final private Function<?, Text> valueToText;

    @Inject(method = "cycle", at = @At("TAIL"))
    public void onClickOption(CallbackInfo ci) {
        InGameTimerUtils.CHANGED_OPTIONS.add(this.valueToText);
    }
}
