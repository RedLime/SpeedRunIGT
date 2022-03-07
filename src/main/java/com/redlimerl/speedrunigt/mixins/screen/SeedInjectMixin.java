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
public class SeedInjectMixin {

    @Shadow private TextFieldWidget seedField;

    @Inject(method = "createLevel", at = @At("HEAD"))
    public void onGenerate(CallbackInfo ci) {
        String string = this.seedField.getText();
        InGameTimerUtils.LATEST_SEED_IS_RANDOM = string.isEmpty();
    }
}
