package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.gui.options.OptionImpl", remap = false)
public class SodiumOptionImplMixin {

    @Inject(method = "setValue", remap = false, at = @At("HEAD"))
    public void onSetValue(CallbackInfo ci) {
        InGameTimerUtils.CHANGED_OPTIONS.add(this);
    }
}
