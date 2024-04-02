package com.redlimerl.speedrunigt.mixins.screen;

import com.llamalad7.mixinextras.sugar.Local;
import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.gui.screen.SpeedRunOptionScreen;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 1))
    private void onInit(CallbackInfo ci, @Local(ordinal = 1) DirectionalLayoutWidget widget) {
        timerButton = widget.add(ButtonWidget.builder(Text.empty(), (buttonWidget) -> {
            if (this.client != null) {
                this.client.setScreen(new SpeedRunOptionScreen(this));
            }
        }).size(20, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.renderEnderPearl(context);
    }

    @Unique
    private void renderEnderPearl(DrawContext context) {
        if (this.client != null) {
            context.getMatrices().push();
            context.getMatrices().translate(-.5f, -.5f, 0);
            context.drawTexture(timerButton.isHovered() ? ENDER_EYE :
                    SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED ? BLAZE_POWDER : ENDER_PEARL, timerButton.getX() + 2, timerButton.getY() + 2, 0.0F, 0.0F, 16, 16, 16, 16);
            context.getMatrices().pop();
        }
    }
}
