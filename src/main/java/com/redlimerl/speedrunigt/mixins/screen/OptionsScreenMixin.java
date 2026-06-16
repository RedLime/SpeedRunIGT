package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunOptionScreen;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {
    private static final Identifier ENDER_PEARL = Identifier.parse("textures/item/ender_pearl.png");
    private static final Identifier BLAZE_POWDER = Identifier.parse("textures/item/blaze_powder.png");
    private static final Identifier ENDER_EYE = Identifier.parse("textures/item/ender_eye.png");

    private Button timerButton;

    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        timerButton = ButtonWidgetHelper.create(this.width / 2 - 180, this.height / 6 - 12, 20, 20, Component.empty(), (buttonWidget) -> {
            if (this.minecraft != null) {
                this.minecraft.gui.setScreen(new SpeedRunOptionScreen(this));
            }
        });
        this.addRenderableWidget(timerButton);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        if (this.minecraft != null) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(-.5f, -.5f);
            graphics.blit(RenderPipelines.GUI_TEXTURED, timerButton.isHovered() ? ENDER_EYE : SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED ? BLAZE_POWDER : ENDER_PEARL,
                    timerButton.getX() + 2, timerButton.getY() + 2, 0.0F, 0.0F, 16, 16, 16, 16);
            graphics.pose().popMatrix();
        }
    }
}
