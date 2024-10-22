package com.redlimerl.speedrunigt.mixins.network;

import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.PracticeTimerManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
    public void onPlayerPositionLookMixin(CallbackInfo ci) {
        if (SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE))
            PracticeTimerManager.stopPractice();
    }

    @Inject(method = "onPlaySound", at = @At("RETURN"))
    public void onPlaySoundIdMixin(PlaySoundS2CPacket packet, CallbackInfo ci) {
        String packetId = packet.getSound().value().id().toString();
        if (packetId.equals("speedrunigt:start_practice")) {
            PracticeTimerManager.startPractice(0);
        }
        if (packetId.equals("speedrunigt:stop_practice")) {
            PracticeTimerManager.stopPractice();
        }
    }

}
