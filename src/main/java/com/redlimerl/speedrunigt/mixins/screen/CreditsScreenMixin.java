package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.client.gui.screen.CreditsScreen;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreditsScreen.class)
public class CreditsScreenMixin {

    @Inject(method = "init()V", at = @At("TAIL"))
    private void initMixin(CallbackInfo ci) {
        @NotNull
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE) {
            if (timer.getCategory() == RunCategories.ANY) {
                InGameTimer.complete();
            }
            RunCategories.checkAllBossesCompleted();
        }
    }
}
