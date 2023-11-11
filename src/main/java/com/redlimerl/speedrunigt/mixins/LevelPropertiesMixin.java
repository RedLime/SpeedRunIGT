package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.instance.GameInstance;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelProperties.class)
public class LevelPropertiesMixin {
    @Inject(method = "setDifficulty", at = @At("TAIL"))
    private void checkPeaceful(Difficulty difficulty, CallbackInfo ci) {
        if (difficulty.equals(Difficulty.PEACEFUL)) {
            GameInstance.getInstance().callEvents("enable_cheats");
        }
    }
}
