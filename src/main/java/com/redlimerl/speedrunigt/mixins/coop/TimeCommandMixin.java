package com.redlimerl.speedrunigt.mixins.coop;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerStartPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TimeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TimeCommand.class)
public class TimeCommandMixin {

    @Inject(method = "setTime", at = @At("RETURN"))
    private static void onSet(CommandSourceStack source, int time, CallbackInfoReturnable<Integer> cir) {
        if (time == 0 && InGameTimer.getInstance().getStatus() != TimerStatus.NONE && InGameTimer.getInstance().isCoop()
        && source.getServer() != null) {
            TimerPacketUtils.sendServer2ClientPacket(source.getServer(), new TimerStartPacket(InGameTimer.getInstance(), 0));
        }
    }
}
