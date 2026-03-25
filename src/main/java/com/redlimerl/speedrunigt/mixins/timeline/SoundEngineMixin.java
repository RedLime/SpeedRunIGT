package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {

    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;", at = @At("RETURN"))
    public void onSoundPlay(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (sound.getIdentifier().equals(SoundEvents.VILLAGER_AMBIENT.location()) && InGameTimer.getInstance().isPlaying()) {
            InGameTimer.getInstance().tryInsertNewTimeline("found_villager");
        }
    }
}
