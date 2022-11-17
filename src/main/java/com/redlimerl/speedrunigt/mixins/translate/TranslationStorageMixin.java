package com.redlimerl.speedrunigt.mixins.translate;

import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Mixin(TranslationStorage.class)
public abstract class TranslationStorageMixin {

    @Shadow protected abstract void method_5946(InputStream stream);

    @Inject(method = "method_19557", at = @At("RETURN"))
    private void onLoad(ResourceManager container, List<String> list, CallbackInfo ci) {
        for (String lang : list) {
            InputStream inputStream = TranslateHelper.setup(lang, SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS));
            if (inputStream == null) continue;
            this.method_5946(inputStream);
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
