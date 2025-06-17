package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunOptionScreen;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {
    private static final Identifier ENDER_PEARL = Identifier.of("textures/item/ender_pearl.png");
    private static final Identifier BLAZE_POWDER = Identifier.of("textures/item/blaze_powder.png");
    private static final Identifier ENDER_EYE = Identifier.of("textures/item/ender_eye.png");

    private ButtonWidget timerButton;

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        timerButton = ButtonWidgetHelper.create(this.width / 2 - 180, this.height / 6 - 12, 20, 20, Text.empty(), (buttonWidget) -> {
            if (this.client != null) {
                this.client.setScreen(new SpeedRunOptionScreen(this));
            }
        });
        this.addDrawableChild(timerButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.client != null) {
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(-.5f, -.5f);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, timerButton.isHovered() ? ENDER_EYE : SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED ? BLAZE_POWDER : ENDER_PEARL,
                    timerButton.getX() + 2, timerButton.getY() + 2, 0.0F, 0.0F, 16, 16, 16, 16);
            context.getMatrices().popMatrix();
        }
    }
}
