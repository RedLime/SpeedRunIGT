package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOptionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SettingsScreen.class)
public class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {

        this.addButton(new TexturedButtonWidget(this.width / 2 - 180, this.height / 6 - 12, 20, 20, 0, 0, 20, SpeedRunIGT.BUTTON_ICON_TEXTURE, 32, 64, (buttonWidget) -> {
            if (this.minecraft != null) {
                this.minecraft.openScreen(new SpeedRunOptionScreen(this));
            }
        }, I18n.translate("ghostrunner.title")));
    }
}
