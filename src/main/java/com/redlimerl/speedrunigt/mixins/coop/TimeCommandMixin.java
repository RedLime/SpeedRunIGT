package com.redlimerl.speedrunigt.mixins.coop;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerPacketHandler;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TimeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TimeCommand.class)
public class TimeCommandMixin {

    @Inject(method = "executeSet", at = @At("RETURN"))
    private static void onSet(ServerCommandSource source, int time, CallbackInfoReturnable<Integer> cir) {
        try {
            if (time == 0 && InGameTimer.getInstance().getStatus() != TimerStatus.NONE && InGameTimer.getInstance().isCoop()
            && source.getPlayer().getServer() != null) {
                TimerPacketHandler.sendInitS2C(source.getPlayer().getServer().getPlayerManager().getPlayerList(), System.currentTimeMillis(), InGameTimer.getInstance().getCategory(), InGameTimer.getInstance().getRunType().getCode());
            }
        } catch (CommandSyntaxException ignored) {
        }
    }
}
