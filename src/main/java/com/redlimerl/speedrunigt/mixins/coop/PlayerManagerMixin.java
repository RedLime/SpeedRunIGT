package com.redlimerl.speedrunigt.mixins.coop;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerInitializePacket;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerStartPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Shadow public abstract int getCurrentPlayerCount();

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "method_12827", at = @At("TAIL"))
    public void onPlayerConnectInject(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        if (this.getCurrentPlayerCount() > (SpeedRunIGT.IS_CLIENT_SIDE ? 1 : 0) && !InGameTimer.getInstance().isCompleted()) {
            if (InGameTimer.getInstance().isStarted() || !SpeedRunIGT.IS_CLIENT_SIDE) {
                long rta = InGameTimer.getInstance().getRealTimeAttack();
                TimerPacketUtils.sendServer2ClientPacket(this.server, new TimerStartPacket(InGameTimer.getInstance(), rta));
            } else {
                TimerPacketUtils.sendServer2ClientPacket(this.server, new TimerInitializePacket(InGameTimer.getInstance()));
            }
        }
    }
}
