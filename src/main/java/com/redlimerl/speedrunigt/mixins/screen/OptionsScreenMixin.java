package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.gui.screen.SpeedRunIGTInfoScreen;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunOptionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SettingsScreen.class)
public class OptionsScreenMixin extends Screen {
    private static final Identifier ENDER_PEARL = new Identifier("textures/items/ender_pearl.png");
    private static final Identifier BLAZE_POWDER = new Identifier("textures/items/blaze_powder.png");
    private static final Identifier ENDER_EYE = new Identifier("textures/items/ender_eye.png");

    private ButtonWidget timerButton;

    @Inject(method = "method_21947", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        timerButton = new ButtonWidget(123456, this.field_22535 / 2 - 180, this.field_22536 / 6 - 12, 20, 20, "");
        field_22537.add(timerButton);
    }

    @Inject(method = "method_21930", at = @At("TAIL"))
    private void onButtonClicked(ButtonWidget button, CallbackInfo ci) {
        if (button == timerButton) {
            if (this.field_22534 != null) {
                this.field_22534.openScreen(new SpeedRunOptionScreen(this));
            }
        }
    }

    @Inject(method = "method_21925", at = @At("TAIL"))
    private void renderEnderPearl(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.field_22534 != null) {
            this.field_22534.getTextureManager().bindTexture(timerButton.method_21885() ? ENDER_EYE :
                    SpeedRunIGTInfoScreen.UPDATE_STATUS == SpeedRunIGTInfoScreen.UpdateStatus.OUTDATED ? BLAZE_POWDER : ENDER_PEARL);
            method_21875(timerButton.x + 2, timerButton.y + 2, 0.0F, 0.0F, 16, 16, 16, 16);
        }
    }
}
