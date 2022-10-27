package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @ModifyArg(method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;<init>(Lnet/minecraft/text/Text;)V"), index = 0)
    private static Text onInit(Text string) {
        if (InGameTimer.getInstance().isStarted() && !InGameTimer.getInstance().isCoop()) {
            return Text.literal(string.getString() + " (#" + (InGameTimer.getInstance().getPauseCount() + 1) + ")");
        } else {
            return string;
        }
    }
}
