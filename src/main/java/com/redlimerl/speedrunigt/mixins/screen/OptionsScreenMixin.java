package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunOptionScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SettingsScreen.class)
public class OptionsScreenMixin extends Screen {
//    n.m.item.Item#method_6324
    private static final String ENDER_PEARL = "ender_pearl";
    private static final String BLAZE_POWDER = "blaze_powder";
    private static final String ENDER_EYE = "ender_eye";

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
                this.client.setScreen(new SpeedRunOptionScreen(this));
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderEnderPearl(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.client != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef(-.5f, -.5f, 0);
            MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.field_6557);
            Sprite texture = ((SpriteAtlasTexture) MinecraftClient.getInstance().getTextureManager().getTexture(SpriteAtlasTexture.field_6557)).getSprite(
                    timerButton.isHovered() ? ENDER_EYE : SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED ? BLAZE_POWDER : ENDER_PEARL
            );
            method_4944(timerButton.x + 2, timerButton.y + 2, texture, 16, 16);
            GL11.glPopMatrix();
        }
    }
}
