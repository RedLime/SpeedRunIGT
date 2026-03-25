package com.redlimerl.speedrunigt.mixins.network;

import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.PracticeTimerManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleMovePlayer", at = @At("RETURN"))
    public void onPlayerPositionLookMixin(CallbackInfo ci) {
        if (SpeedRunOption.getOption(SpeedRunOptions.TELEPORT_TO_END_PRACTICE))
            PracticeTimerManager.stopPractice();
    }

    @Inject(method = "handleSoundEvent", at = @At("RETURN"))
    public void onPlaySoundIdMixin(ClientboundSoundPacket packet, CallbackInfo ci) {
        String packetId = packet.getSound().value().location().toString();
        if (packetId.equals("speedrunigt:start_practice")) {
            PracticeTimerManager.startPractice(0);
        }
        if (packetId.equals("speedrunigt:stop_practice")) {
            PracticeTimerManager.stopPractice();
        }
    }

}
