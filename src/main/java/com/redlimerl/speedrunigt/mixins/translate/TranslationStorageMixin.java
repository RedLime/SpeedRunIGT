package com.redlimerl.speedrunigt.mixins.translate;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.utils.TranslateHelper;
import net.minecraft.util.Language;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

@Mixin(Language.class)
public abstract class TranslationStorageMixin {

    @Shadow
    protected abstract void method_633(Properties properties, String string);

    @Redirect(method = "setCode", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Language;method_633(Ljava/util/Properties;Ljava/lang/String;)V"))
    private void onLoad(Language instance, Properties properties, String language) {
        method_633(properties, language);
        InputStream inputStream = TranslateHelper.setup(language, SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS));
        try {
            if (inputStream == null) return;
            String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

            JsonElement jsonElement = new JsonParser().parse(json);

            for (Map.Entry<String, JsonElement> stringJsonElementEntry : jsonElement.getAsJsonObject().entrySet()) {
                properties.setProperty(stringJsonElementEntry.getKey(), stringJsonElementEntry.getValue().getAsString());
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Redirect(method = "setCode", at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean alwaysRefreshLanguageKeys(String instance, Object object) {
        return false;
    }
}
