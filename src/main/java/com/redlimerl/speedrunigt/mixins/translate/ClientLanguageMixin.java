package com.redlimerl.speedrunigt.mixins.translate;

import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

    @Inject(method = "appendFrom(Ljava/lang/String;Ljava/util/List;Ljava/util/Map;)V", at = @At("RETURN"))
    private static void onLoad(String langCode, List<Resource> resourceRefs, Map<String, String> translations, CallbackInfo ci) {
        TranslateHelper.setup(langCode, translations::put, SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS));
    }
}
