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

    @Inject(method = "onCustomPayload", at = @At("TAIL"), cancellable = true)
    public void onCustom(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (Objects.equals(packet.method_7733().getNamespace(), SpeedRunIGT.MOD_ID)) {
            SpeedRunIGT.debug("Client Side : " + packet.getPayload().toString());

            if (Objects.equals(packet.method_7733().getPath(), TimerPacketHandler.PACKET_TIMER_INIT_ID.getPath())) {
                TimerPacketHandler.receiveInitS2C(packet.getPayload());
            }

            if (Objects.equals(packet.method_7733().getPath(), TimerPacketHandler.PACKET_TIMER_COMPLETE_ID.getPath())) {
                TimerPacketHandler.receiveCompleteS2C(packet.getPayload());
            }

            if (Objects.equals(packet.method_7733().getPath(), TimerPacketHandler.PACKET_TIMER_SPLIT_ID.getPath())) {
                TimerPacketHandler.receiveSplitS2C(packet.getPayload());
            }

            ci.cancel();
        }
    }
}
