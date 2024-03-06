package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunOptionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SettingsScreen.class)
public class OptionsScreenMixin extends Screen {
//    n.m.item.Item#getTexture
    private static final int ENDER_PEARL = Item.ENDER_PEARL.method_3369(0, 0);
    private static final int BLAZE_POWDER = Item.BLAZE_POWDER.method_3369(0, 0);
    private static final int ENDER_EYE = Item.EYE_OF_ENDER.method_3369(0, 0);

    private ButtonWidget timerButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        timerButton = new ButtonWidget(123456, this.width / 2 - 180, this.height / 6 - 12, 20, 20, "");
        buttons.add(timerButton);
    }

    @Inject(method = "buttonClicked", at = @At("TAIL"))
    private void onButtonClicked(ButtonWidget button, CallbackInfo ci) {
        if (button == timerButton) {
            if (this.field_1229 != null) {
                this.field_1229.openScreen(new SpeedRunOptionScreen(this));
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderEnderPearl(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.field_1229 != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef(-.5f, -.5f, 0);
            field_1229.textureManager.bindTexture(field_1229.textureManager.getTextureFromPath("/gui/items.png"));
            int id = timerButton.isHovered() ? ENDER_EYE : SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED ? BLAZE_POWDER : ENDER_PEARL;
            drawTexture(timerButton.x + 2, timerButton.y + 2, id % 16 * 16, id / 16 * 16, 16, 16);
            GL11.glPopMatrix();
        }
    }
}
