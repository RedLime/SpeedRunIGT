package com.redlimerl.speedrunigt.mixins.network;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onCustomPayload", at = @At("TAIL"))
    public void onCustom(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (packet.getChannel().startsWith("srigt")) {
            TimerPacket timerPacket = TimerPacket.createTimerPacketFromPacket(packet.getChannel());
            TimerPacketBuf buf = TimerPacketBuf.of(packet.getPayload());
            SpeedRunIGT.debug(String.format("Client->Server Packet: %s bytes, ID : %s", buf.getBuffer().capacity(), packet.getChannel()));
            try {
                if (timerPacket != null) timerPacket.receiveClient2ServerPacket(buf, server);
            } catch (Exception e) {
                e.printStackTrace();
                SpeedRunIGT.error("Failed to read packet in server side, probably SpeedRunIGT version different between players");
            }
        }
    }
}
