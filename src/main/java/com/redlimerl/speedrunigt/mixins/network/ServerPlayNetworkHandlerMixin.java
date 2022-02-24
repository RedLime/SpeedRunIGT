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

    @Inject(method = "onCustomPayload", at = @At("TAIL"))
    public void onCustom(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (packet.getChannel().startsWith(SpeedRunIGT.MOD_ID+"|")) {
            SpeedRunIGT.debug("Server Side : " + packet.getChannel());

            if (Objects.equals(packet.getChannel(), TimerPacketHandler.PACKET_TIMER_INIT_ID)) {
                TimerPacketHandler.receiveInitC2S(this.server, packet.getPayload());
            }

            if (Objects.equals(packet.getChannel(), TimerPacketHandler.PACKET_TIMER_COMPLETE_ID)) {
                TimerPacketHandler.receiveCompleteC2S(this.server, packet.getPayload());
            }

            if (Objects.equals(packet.getChannel(), TimerPacketHandler.PACKET_TIMER_SPLIT_ID)) {
                TimerPacketHandler.receiveSplitC2S(this.server, packet.getPayload());
            }
        }
    }
}
