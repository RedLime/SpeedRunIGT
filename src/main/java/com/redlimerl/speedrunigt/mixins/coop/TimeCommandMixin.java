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

    @Shadow
    protected abstract void method_14(CommandSource commandSource, int i);

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/TimeCommand;method_14(Lnet/minecraft/command/CommandSource;I)V"))
    private void onExecute(TimeCommand instance, CommandSource i1, int i2) {
        this.method_14(i1, i2);
        if (i2 == 0 && InGameTimer.getInstance().getStatus() != TimerStatus.NONE && InGameTimer.getInstance().isCoop()) {
            TimerPacketHandler.sendInitS2C(MinecraftServer.getServer().getPlayerManager().getPlayers(), System.currentTimeMillis(), InGameTimer.getInstance().getCategory(), InGameTimer.getInstance().getSeedName(), InGameTimer.getInstance().isSetSeed());
        }
    }
}
