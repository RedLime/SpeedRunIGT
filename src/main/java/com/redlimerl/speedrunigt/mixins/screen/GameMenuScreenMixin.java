package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @ModifyArg(method = "render",
            slice = @Slice(from = @At("HEAD"), to = @At("TAIL")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;drawCenteredString(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"), index = 1)
    public String onRender(String string) {
        if (InGameTimer.getInstance().isPaused() && InGameTimer.getInstance().isStarted()) {
            return string + " (#" + InGameTimer.getInstance().getPauseCount() + ")";
        } else {
            return string;
        }
    }
}
