package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(CycleButton.class)
public class OptionButtonWidgetMixin {

    @Shadow @Final private Function<?, Component> valueStringifier;

    @Inject(method = "cycleValue", at = @At("TAIL"))
    public void onClickOption(CallbackInfo ci) {
        InGameTimerUtils.CHANGED_OPTIONS.add(this.valueStringifier);
    }
}
