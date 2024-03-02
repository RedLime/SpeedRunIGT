package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunOptionScreen;
import net.minecraft.class_1015;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {
    private static final Identifier ENDER_PEARL = new Identifier("textures/items/ender_pearl.png");
    private static final Identifier BLAZE_POWDER = new Identifier("textures/items/blaze_powder.png");
    private static final Identifier ENDER_EYE = new Identifier("textures/items/ender_eye.png");

    private ClickableWidget timerButton;

    @Inject(method = "method_2214", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        timerButton = new ClickableWidget(123456, this.field_2561 / 2 - 180, this.field_2559 / 6 - 12, 20, 20, "");
        field_2564.add(timerButton);
    }

    @Inject(method = "method_0_2778", at = @At("TAIL"))
    private void onButtonClicked(ClickableWidget button, CallbackInfo ci) {
        if (button == timerButton) {
            if (this.field_2563 != null) {
                this.field_2563.setScreen(new SpeedRunOptionScreen(this));
            }
        }
    }

    @Inject(method = "method_2214", at = @At("TAIL"))
    private void renderEnderPearl(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.field_2563 != null) {
            class_1015.method_4461();
            class_1015.method_4348(-.5f, -.5f, 0);
            this.field_2563.getTextureManager().bindTextureInner(timerButton.method_1828() ? ENDER_EYE :
                    SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED ? BLAZE_POWDER : ENDER_PEARL);
            method_1781(timerButton.field_2069 + 2, timerButton.field_2068 + 2, 0.0F, 0.0F, 16, 16, 16, 16);
            class_1015.method_4350();
        }
    }
}
