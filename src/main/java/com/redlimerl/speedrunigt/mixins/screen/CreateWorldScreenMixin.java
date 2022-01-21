package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {

    @Shadow
    protected abstract <T extends AbstractButtonWidget> T addButton(T button);

    private ButtonWidget fsgButton;

    protected CreateWorldScreenMixin(Text title) {
        super(title);
    }


    @Inject(method = "init", at = @At("HEAD"))
    private void initButton(CallbackInfo ci) {
        this.fsgButton = addButton(new ButtonWidget(this.width / 2 + 5, 151, 150, 20,
                new TranslatableText("speedrunigt.message.is_fsg").append(": ").append(SpeedRunIGT.LATEST_IS_FSG ? ScreenTexts.YES : ScreenTexts.NO), (buttonWidget) -> {
            SpeedRunIGT.LATEST_IS_FSG = !SpeedRunIGT.LATEST_IS_FSG;
            buttonWidget.setMessage(new TranslatableText("speedrunigt.message.is_fsg").append(": ").append(SpeedRunIGT.LATEST_IS_FSG ? ScreenTexts.YES : ScreenTexts.NO));
        }));
        this.fsgButton.visible = false;
    }

    @Inject(method = "setMoreOptionsOpen(Z)V", at = @At("HEAD"))
    private void toggleMoreOption(boolean b, CallbackInfo ci) {
        this.fsgButton.visible = b;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/world/MoreOptionsDialog;render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V"))
    private void moreOptionRenderInject(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        drawStringWithShadow(matrices, this.textRenderer, I18n.translate("speedrunigt.message.is_fsg.description"), this.width / 2 + 5, 172, -6250336);
    }

    @Override
    public boolean changeFocus(boolean lookForwards) {
        boolean result = super.changeFocus(lookForwards);
        while (getFocused() == this.fsgButton) {
            result = super.changeFocus(lookForwards);
        }
        return result;
    }
}
