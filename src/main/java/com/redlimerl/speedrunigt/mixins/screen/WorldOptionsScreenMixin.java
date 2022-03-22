package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public class WorldOptionsScreenMixin {

    @Shadow private TextFieldWidget seedField;

    @Inject(method = "method_18847", at = @At("HEAD"))
    public void onGenerate(CallbackInfo ci) {
        InGameTimerUtils.IS_SET_SEED = !this.seedField.getText().isEmpty();
    }
}
