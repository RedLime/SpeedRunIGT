package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.utils.MixinValues;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "method_9775", at = @At(value = "TAIL"))
    private void drawTimer(CallbackInfo ci) {
        MixinValues.IS_RENDERED_BEFORE = true;
    }
}
