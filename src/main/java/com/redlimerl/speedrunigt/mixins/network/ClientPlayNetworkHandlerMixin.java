package com.redlimerl.speedrunigt.mixins.network;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Shadow private MinecraftClient client;

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    public void onCustom(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (packet.getChannel().startsWith(SpeedRunIGT.MOD_ID)) {
            TimerPacket timerPacket = TimerPacket.createTimerPacketFromPacket(packet.getChannel());
            TimerPacketBuf buf = TimerPacketBuf.of(Unpooled.wrappedBuffer(packet.method_7734()));
            SpeedRunIGT.debug(String.format("Server->Client Packet: %s bytes, ID : %s", buf.getBuffer().capacity(), packet.getChannel()));
            try {
                if (timerPacket != null && SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) {
                    timerPacket.receiveServer2ClientPacket(buf, client);
                    buf.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
                SpeedRunIGT.error("Failed to read packet in client side, probably SpeedRunIGT version different between players");
            } finally {
                ci.cancel();
            }
        }
    }
}
