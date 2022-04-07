package com.redlimerl.speedrunigt.mixins.coop;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerInitPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
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

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    public void onPlayerConnectInject(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        if (InGameTimer.getInstance().isStarted() && !InGameTimer.getInstance().isCompleted() && this.getCurrentPlayerCount() > 1) {
            TimerPacketUtils.sendServer2ClientPacket(this.server, new TimerInitPacket(InGameTimer.getInstance(), InGameTimer.getInstance().getStartTime()));
        }
    }
}
