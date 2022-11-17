package com.redlimerl.speedrunigt.mixins.translate;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Mixin(TranslationStorage.class)
public abstract class TranslationStorageMixin {

    @Shadow
    Map<String, String> translations;

    @Inject(method = "method_5945", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resource/language/TranslationStorage;method_5950()V", shift = At.Shift.BEFORE))
    private void onLoad(ResourceManager container, List<String> list, CallbackInfo ci) {
        for (String lang : list) {
            InputStream inputStream = TranslateHelper.setup(lang, SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS));
            try {
                if (inputStream == null) continue;
                String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

                JsonElement jsonElement = new JsonParser().parse(json);

                for (Map.Entry<String, JsonElement> stringJsonElementEntry : jsonElement.getAsJsonObject().entrySet()) {
                    this.translations.put(stringJsonElementEntry.getKey(), stringJsonElementEntry.getValue().getAsString());
                }
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
