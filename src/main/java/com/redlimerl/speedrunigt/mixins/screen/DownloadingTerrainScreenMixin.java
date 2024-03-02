package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DownloadingTerrainScreen.class)
public abstract class DownloadingTerrainScreenMixin extends Screen {

    @Override
    public void method_2224() {
        super.method_2224();
        InGameTimer timer = InGameTimer.getInstance();
        if (field_2563 != null && field_2563.isIntegratedServerRunning() && !timer.isCoop() && timer.getStatus() != TimerStatus.IDLE) {
            timer.setPause(true, TimerStatus.IDLE, "dimension load?");
            InGameTimerUtils.IS_CHANGING_DIMENSION = false;
        }
    }

    @ModifyArg(method = "method_2214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen;method_1789(Lnet/minecraft/class_0_681;Ljava/lang/String;III)V"), index = 1)
    public String onRender(String string) {
        if (InGameTimer.getInstance().isPaused() && InGameTimer.getInstance().isStarted() && !InGameTimer.getInstance().isCoop()) {
            return string + " (#" + InGameTimer.getInstance().getPauseCount() + ")";
        } else {
            return string;
        }
    }
}
