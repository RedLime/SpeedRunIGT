package com.redlimerl.speedrunigt.mixins.coop;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerPacketHandler;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.TimeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TimeCommand.class)
public abstract class TimeCommandMixin {

    @Shadow protected abstract void method_12478(MinecraftServer minecraftServer, int i);

    @Redirect(method = "method_3279", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/TimeCommand;method_12478(Lnet/minecraft/server/MinecraftServer;I)V"))
    private void onExecute(TimeCommand instance, MinecraftServer minecraftServer, int time) {
        this.method_12478(minecraftServer, time);
        if (time == 0 && InGameTimer.getInstance().getStatus() != TimerStatus.NONE && InGameTimer.getInstance().isCoop()) {
            TimerPacketHandler.sendInitS2C(minecraftServer.getPlayerManager().getPlayers(), System.currentTimeMillis(), InGameTimer.getInstance().getCategory());
        }
    }
}
