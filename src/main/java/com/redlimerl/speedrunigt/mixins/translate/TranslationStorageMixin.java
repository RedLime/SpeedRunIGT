package com.redlimerl.speedrunigt.mixins.translate;

import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.utils.ResourcesHelper;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(TranslationStorage.class)
public class TranslationStorageMixin {

    @Inject(method = "load(Ljava/util/List;Ljava/util/Map;)V", at = @At("RETURN"))
    private static void onLoad(List<Resource> resources, Map<String, String> translationMap, CallbackInfo ci) {
        resources.forEach(resource -> {
            // minecraft always loads en_us as a backup, if using only english translations just skip loading the other attempts
            if (SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS) && !resource.getId().getPath().equalsIgnoreCase("lang/en_us.json"))
                return;
            Optional.ofNullable(ResourcesHelper.toStream("/assets/speedrunigt/" + resource.getId().getPath()))
                    .ifPresent(langStream -> Language.load(langStream, translationMap::put));
        });
    }
}
