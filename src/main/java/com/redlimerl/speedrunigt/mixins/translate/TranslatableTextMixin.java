package com.redlimerl.speedrunigt.mixins.translate;

import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TranslatableTextContent.class)
public class TranslatableTextMixin {

    @Shadow @Final private String key;

    @ModifyVariable(method = "updateTranslations", at = @At("STORE"), ordinal = 0)
    private String injected(String string) {
        return TranslateHelper.hasTranslate(this.key) ? TranslateHelper.translate(this.key) : string;
    }
}
