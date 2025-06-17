package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;", at = @At("RETURN"))
    public void onSoundPlay(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        if (sound.getId().equals(SoundEvents.ENTITY_VILLAGER_AMBIENT.id()) && InGameTimer.getInstance().isPlaying()) {
            InGameTimer.getInstance().tryInsertNewTimeline("found_villager");
        }
    }
}
