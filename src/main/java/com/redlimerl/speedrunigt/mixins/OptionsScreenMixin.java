package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOptionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {

        this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 180, this.height / 6 - 12, 20, 20, 0, 0, 20, SpeedRunIGT.BUTTON_ICON_TEXTURE, 32, 64, (buttonWidget) -> {
            if (this.client != null) {
                this.client.setScreen(new SpeedRunOptionScreen(this));
            }
        }, new TranslatableText("speedrunigt.title")));
    }
}
