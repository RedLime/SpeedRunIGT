package com.redlimerl.speedrunigt.mixins.translate;

import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.minecraft.client.resource.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(I18n.class)
public class I18nMixin {
    @Inject(method = "translate", at = @At("HEAD"), cancellable = true)
    private static void translateInject(String key, Object[] args, CallbackInfoReturnable<String> cir) {
        if (TranslateHelper.hasTranslate(key))
            cir.setReturnValue(String.format(TranslateHelper.translate(key), args));
    }
}
