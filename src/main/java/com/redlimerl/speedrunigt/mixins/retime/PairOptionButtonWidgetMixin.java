package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.class_1805;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1805.class)
public class PairOptionButtonWidgetMixin {

    @Shadow @Final private ButtonWidget field_7731;

    @Inject(method = "method_6699", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/options/GameOptions;getStringOption(Lnet/minecraft/client/options/GameOption;)Ljava/lang/String;", shift = At.Shift.AFTER))
    public void onClickOption(int j, int k, int l, int m, int n, int par6, CallbackInfoReturnable<Boolean> cir) {
        InGameTimerUtils.CHANGED_OPTIONS.add(((OptionButtonWidget)this.field_7731).method_1088());
    }
}
