package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunOptionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {
    private static final Identifier ENDER_PEARL = new Identifier("textures/item/ender_pearl.png");
    private static final Identifier BLAZE_POWDER = new Identifier("textures/item/blaze_powder.png");
    private static final Identifier ENDER_EYE = new Identifier("textures/item/ender_eye.png");

    private ButtonWidget timerButton;

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.timerButton = this.addButton(new ButtonWidget(this.width / 2 - 180, this.height / 6 - 12, 20, 20, new LiteralText(""), (buttonWidget) -> {
            if (this.client != null) {
                this.client.openScreen(new SpeedRunOptionScreen(this));
            }
        }));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderEnderPearl(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.client != null) {
            this.client.getTextureManager().bindTexture(
                    this.timerButton.isHovered()
                            ? ENDER_EYE
                            : SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED
                                    ? BLAZE_POWDER
                                    : ENDER_PEARL
            );
            drawTexture(matrices, this.timerButton.x + 2, this.timerButton.y + 2, 0.0F, 0.0F, 16, 16, 16, 16);
        }
    }
}
