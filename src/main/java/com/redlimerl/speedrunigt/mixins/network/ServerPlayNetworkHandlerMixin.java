package com.redlimerl.speedrunigt.mixins.network;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.TimerPacketHandler;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    public void onCustom(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        CustomPayloadC2SPacketAccessor customPacket = (CustomPayloadC2SPacketAccessor) packet;
        if (Objects.equals(customPacket.getChannel().getNamespace(), SpeedRunIGT.MOD_ID)) {
            if (Objects.equals(customPacket.getChannel().getPath(), TimerPacketHandler.PACKET_TIMER_ID.getPath())) {
                TimerPacketHandler.serverReceiveAndSend(this.server, customPacket.getData());
            }
        }
    }
}
