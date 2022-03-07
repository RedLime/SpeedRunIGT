package com.redlimerl.speedrunigt.mixins.coop;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerPacketHandler;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.TimeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TimeCommand.class)
public class TimeCommandMixin {

    @Inject(method = "method_12478", at = @At("RETURN"))
    private void onSet(MinecraftServer minecraftServer, int time, CallbackInfo ci) {
        if (time == 0 && InGameTimer.getInstance().getStatus() != TimerStatus.NONE && InGameTimer.getInstance().isCoop()) {
            TimerPacketHandler.sendInitS2C(minecraftServer.getPlayerManager().getPlayers(), System.currentTimeMillis(), InGameTimer.getInstance().getCategory());
        }
    }
}
