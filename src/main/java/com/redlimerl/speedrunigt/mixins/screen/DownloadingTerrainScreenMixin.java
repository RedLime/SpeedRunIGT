package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DownloadingTerrainScreen.class)
public abstract class DownloadingTerrainScreenMixin extends Screen {

    @Override
    public void method_21947() {
        super.method_21947();
        InGameTimer timer = InGameTimer.getInstance();
        if (field_22534 != null && field_22534.isInSingleplayer() && !timer.isCoop() && timer.getStatus() != TimerStatus.IDLE) {
            timer.setPause(true, TimerStatus.IDLE, "dimension load?");
        }
    }

    @ModifyArg(method = "method_21925", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen;method_21881(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"), index = 1)
    public String onRender(String string) {
        if (InGameTimer.getInstance().isPaused() && InGameTimer.getInstance().isStarted() && !InGameTimer.getInstance().isCoop()) {
            return string + " (#" + InGameTimer.getInstance().getPauseCount() + ")";
        } else {
            return string;
        }
    }
}
