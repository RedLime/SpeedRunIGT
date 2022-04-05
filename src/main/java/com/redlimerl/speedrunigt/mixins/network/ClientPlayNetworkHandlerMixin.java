package com.redlimerl.speedrunigt.mixins.network;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.TimerPacketHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    public void onCustom(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (Objects.equals(packet.getChannel().getNamespace(), SpeedRunIGT.MOD_ID)) {
            if (Objects.equals(packet.getChannel().getPath(), TimerPacketHandler.PACKET_TIMER_ID.getPath())) {
                TimerPacketHandler.clientReceive(packet.getData());
            }
            if (Objects.equals(packet.getChannel().getPath(), TimerPacketHandler.PACKET_ADVANCEMENT_ID.getPath())) {
                TimerPacketHandler.clientAdvancementReceive(packet.getData());
            }
        }
    }
}
