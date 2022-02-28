package com.redlimerl.speedrunigt.mixins.network;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.TimerPacketHandler;
import io.netty.buffer.Unpooled;
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
        if (packet.getChannel().startsWith(SpeedRunIGT.MOD_ID)) {
            SpeedRunIGT.debug("Client Side : " + packet.getChannel());

            if (Objects.equals(packet.getChannel(), TimerPacketHandler.PACKET_TIMER_INIT_ID)) {
                TimerPacketHandler.receiveInitS2C(Unpooled.wrappedBuffer(packet.method_7734()));
            }

            if (Objects.equals(packet.getChannel(), TimerPacketHandler.PACKET_TIMER_COMPLETE_ID)) {
                TimerPacketHandler.receiveCompleteS2C(Unpooled.wrappedBuffer(packet.method_7734()));
            }

            ci.cancel();
        }
    }
}
