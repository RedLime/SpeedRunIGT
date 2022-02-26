package com.redlimerl.speedrunigt.mixins.coop;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerPacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Shadow public abstract int getCurrentPlayerCount();

    @Shadow public abstract List<ServerPlayerEntity> getPlayers();

    @Inject(method = "method_12827", at = @At("TAIL"))
    public void onPlayerConnectInject(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        if (InGameTimer.getInstance().isStarted() && !InGameTimer.getInstance().isCompleted() && this.getCurrentPlayerCount() > 1) {
            TimerPacketHandler.sendInitS2C(this.getPlayers(), InGameTimer.getInstance().getStartTime(), InGameTimer.getInstance().getCategory(), InGameTimer.getInstance().getTimerSplit().getSeed(), InGameTimer.getInstance().getTimerSplit().getRunType());
        }
    }
}
