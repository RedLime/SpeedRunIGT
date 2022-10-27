package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalLong;

@Mixin(GeneratorOptions.class)
public class GeneratorOptionsMixin {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Inject(method = "withSeed", at = @At("HEAD"))
    public void onGenerate(OptionalLong optionalLong, CallbackInfoReturnable<GeneratorOptions> cir) {
        InGameTimerUtils.IS_SET_SEED = optionalLong.isPresent();
    }
}
