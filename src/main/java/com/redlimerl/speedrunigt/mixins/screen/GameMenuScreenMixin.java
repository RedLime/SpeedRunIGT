package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;drawCenteredText(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"), index = 2)
    public Text onRender(Text string) {
        if (InGameTimer.getInstance().isPaused() && InGameTimer.getInstance().isStarted()) {
            return new LiteralText(string.getString() + " (#" + InGameTimer.getInstance().getPauseCount() + ")");
        } else {
            return string;
        }
    }
}
