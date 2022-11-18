package com.redlimerl.speedrunigt.mixins.translate;

import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(TranslationStorage.class)
public class TranslationStorageMixin {
    @Inject(method = "load(Ljava/util/List;Ljava/util/Map;)V", at = @At("RETURN"))
    private static void onLoad(List<Resource> resources, Map<String, String> translationMap, CallbackInfo ci) {
        TranslateHelper.setup(resources, translationMap::put, SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS));
    }
}
