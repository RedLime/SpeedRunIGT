package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screens.WorldOptionsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(WorldOptionsScreen.class)
public class OptionScreenMixin {

    @Shadow
    @Final
    private WorldOptionsScreen.DifficultyButtons difficultyButtons;

    @Inject(method = "onDifficultyChanged", remap = false, at = @At("TAIL"))
    private void onChangeDifficulty(CallbackInfo ci) {
        InGameTimerUtils.CHANGED_OPTIONS.add(Objects.requireNonNull(this.difficultyButtons).difficultyButton());
    }
}
