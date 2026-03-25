package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {

    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @ModifyArg(method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;<init>(Lnet/minecraft/network/chat/Component;)V"), index = 0)
    private static Component onInit(Component string) {
        if (InGameTimer.getInstance().isStarted() && !InGameTimer.getInstance().isCoop()) {
            return Component.literal(string.getString() + " (#" + (InGameTimer.getInstance().getPauseCount() + 1) + ")");
        } else {
            return string;
        }
    }
}
