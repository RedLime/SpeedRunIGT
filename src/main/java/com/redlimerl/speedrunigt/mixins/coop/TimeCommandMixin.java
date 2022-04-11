package com.redlimerl.speedrunigt.mixins.coop;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerInitPacket;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.TimeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TimeCommand.class)
public abstract class TimeCommandMixin {

    @Inject(method = "method_14", at = @At("TAIL"))
    private void onExecute(CommandSource commandSource, int i, CallbackInfo ci) {
        if (i == 0 && InGameTimer.getInstance().getStatus() != TimerStatus.NONE && InGameTimer.getInstance().isCoop()) {
            TimerPacketUtils.sendServer2ClientPacket(MinecraftServer.getServer(), new TimerInitPacket(InGameTimer.getInstance(), 0));
        }
    }
}
