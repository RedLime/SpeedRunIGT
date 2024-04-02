package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.class_9604;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_9604.class)
public abstract class PotatoScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void checkPotatoCategoryCriteria(CallbackInfo ci) {
        if (InGameTimer.getInstance().getCategory() == RunCategories.POTATO) {
            InGameTimer.complete();
        }
    }
}
