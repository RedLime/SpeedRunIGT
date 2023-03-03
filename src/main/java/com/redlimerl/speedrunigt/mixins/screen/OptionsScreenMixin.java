package com.redlimerl.speedrunigt.mixins.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
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

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        timerButton = new ButtonWidget(123456, this.width / 2 - 180, this.height / 6 - 12, 20, 20, "");
        buttons.add(timerButton);
    }

    @Inject(method = "buttonClicked", at = @At("TAIL"))
    private void onButtonClicked(ButtonWidget button, CallbackInfo ci) {
        if (button == timerButton) {
            if (this.client != null) {
                this.client.openScreen(new SpeedRunOptionScreen(this));
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderEnderPearl(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.client != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(-.5f, -.5f, 0);
            this.client.getTextureManager().bindTexture(timerButton.isHovered() ? ENDER_EYE :
                    SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED ? BLAZE_POWDER : ENDER_PEARL);
            drawTexture(timerButton.x + 2, timerButton.y + 2, 0.0F, 0.0F, 16, 16, 16, 16);
            GlStateManager.popMatrix();
        }
    }
}
