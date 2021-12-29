package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.option.SpeedRunOptionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SettingsScreen.class)
public class SettingsScreenMixin extends Screen {

    protected SettingsScreenMixin() {
        super();
    }
    /**
     * @author Void_X_Walker
     * @reason Backported to 1.8, 1.8 buttons work with ids and a separated method instead of pressActions
     */
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {

        this.buttons.add(new ButtonWidget(7797,this.width / 2 - 180, this.height / 6 - 12, 20, 20,"T"));
    }
    @Inject(method = "buttonClicked", at = @At("TAIL"))
    public void buttonClicked(ButtonWidget button, CallbackInfo ci){
        if(button.id==7797){
            if (this.client != null) {
                this.client.openScreen(new SpeedRunOptionScreen(this));
            }
        }
    }
}
