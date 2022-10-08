package com.redlimerl.speedrunigt.mixins.network;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.PracticeTimerManager;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.minecraft.class_2913;
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
        if (packet.getChannel().startsWith("srigt")) {
            TimerPacket timerPacket = TimerPacket.createTimerPacketFromPacket(packet.getChannel());
            TimerPacketBuf buf = TimerPacketBuf.of(packet.getPayload());
            SpeedRunIGT.debug(String.format("Server->Client Packet: %s bytes, ID : %s", buf.getBuffer().capacity(), packet.getPayload()));
            try {
                if (timerPacket != null && SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) {
                    timerPacket.receiveServer2ClientPacket(buf, client);
                    buf.release();
                }
                else throw new IllegalArgumentException();
            } catch (IllegalArgumentException e) {
                SpeedRunIGT.error("Unknown packet type, probably SpeedRunIGT version different between players");
            } catch (Exception e) {
                e.printStackTrace();
                SpeedRunIGT.error("Failed to read packet in client side, probably SpeedRunIGT version different between players");
            } finally {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
    public void onPlayerPositionLookMixin(CallbackInfo ci) {
        if (SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE))
            PracticeTimerManager.stopPractice();
    }

    @Inject(method = "method_12609", at = @At("RETURN"))
    public void onPlaySoundIdMixin(class_2913 packet, CallbackInfo ci) {
        String packetId = packet.method_12644();
        if (packetId.equals("speedrunigt:start_practice")) {
            PracticeTimerManager.startPractice(0);
        }
        if (packetId.equals("speedrunigt:stop_practice")) {
            PracticeTimerManager.stopPractice();
        }
    }

}
