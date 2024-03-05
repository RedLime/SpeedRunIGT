package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Inject(method = "playSound(Ljava/lang/String;FFFFF)V", at = @At("RETURN"))
    public void onSoundPlay(String x, float y, float z, float volume, float pitch, float par6, CallbackInfo ci) {
        if (x.equals("mob.villager.idle") && InGameTimer.getInstance().isPlaying()) {
            InGameTimer.getInstance().tryInsertNewTimeline("found_villager");
        }
    }
}
