package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.Sounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("RETURN"))
    public void onSoundPlay(SoundInstance soundInstance, CallbackInfo ci) {
        if (soundInstance.getIdentifier().equals(Sounds.ENTITY_VILLAGER_AMBIENT.getId()) && InGameTimer.getInstance().isPlaying()) {
            InGameTimer.getInstance().tryInsertNewTimeline("found_villager");
        }
    }
}
