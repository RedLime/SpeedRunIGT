package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(remap = false, targets = "me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI")
public class SodiumOptionsGUIMixin {

    @Inject(method = "applyChanges", at = @At("TAIL"), remap = false)
    public void onChanges(CallbackInfo ci) {
        InGameTimerUtils.RETIME_CHANGED_OPTION_COUNT++;
    }
}
