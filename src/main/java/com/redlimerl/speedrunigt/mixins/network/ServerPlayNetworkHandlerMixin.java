package com.redlimerl.speedrunigt.mixins.network;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerPacketListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

@Mixin(ServerPacketListener.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onCustomPayload", at = @At("TAIL"))
    public void onCustom(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (packet.channel.startsWith("srigt")) {
            TimerPacket timerPacket = TimerPacket.createTimerPacketFromPacket(packet.channel);
            SpeedRunIGT.debug(String.format("Client->Server Packet: %s bytes, ID : %s", packet.field_2455.length, packet.channel));
            try {
                if (timerPacket != null) timerPacket.receiveClient2ServerPacket(packet, this.server, packet.field_2455);
            } catch (Exception e) {
                e.printStackTrace();
                SpeedRunIGT.error("Failed to read packet in server side, probably SpeedRunIGT version different between players");
            }
        }
    }
}
