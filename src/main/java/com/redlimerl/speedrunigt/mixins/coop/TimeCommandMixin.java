package com.redlimerl.speedrunigt.mixins.coop;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerPacketHandler;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.class_3915;
import net.minecraft.server.command.TimeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TimeCommand.class)
public class TimeCommandMixin {

    @Inject(method = "method_21122", at = @At("RETURN"))
    private static void onSet(class_3915 source, int time, CallbackInfoReturnable<Integer> cir) {
        if (time == 0 && InGameTimer.getInstance().getStatus() != TimerStatus.NONE && InGameTimer.getInstance().isCoop()
        && source.method_17473() != null) {
            TimerPacketHandler.sendInitS2C(source.method_17473().getPlayerManager().getPlayers(), System.currentTimeMillis(), InGameTimer.getInstance().getCategory(), InGameTimer.getInstance().getRunType().getCode());
        }
    }
}
