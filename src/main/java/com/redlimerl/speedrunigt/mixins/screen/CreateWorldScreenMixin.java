package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {

    @Shadow private TextFieldWidget seedField;

    @Inject(method = "buttonClicked", at = @At("HEAD"))
    public void onLoadOptions(CallbackInfo ci) {
        String string = this.seedField.getText();
        SpeedRunIGT.LATEST_IS_SSG = !StringUtils.isEmpty(string);
    }
}
