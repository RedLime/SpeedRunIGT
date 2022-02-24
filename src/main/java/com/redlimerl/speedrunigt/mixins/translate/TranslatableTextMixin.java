package com.redlimerl.speedrunigt.mixins.translate;

import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Language.class)
public class TranslatableTextMixin {

    @Inject(method = "translateNullSafe", at = @At("HEAD"), cancellable = true)
    private void injected(String string, CallbackInfoReturnable<String> cir) {
        if (TranslateHelper.hasTranslate(string)) {
            cir.setReturnValue(TranslateHelper.translate(string));
        }
    }

}
