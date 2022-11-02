package com.redlimerl.speedrunigt.mixins.translate;

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

    @Shadow protected abstract void load(InputStream inputStream);

    @Inject(method = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;)V", at = @At("RETURN"))
    private void onLoad(ResourceManager container, List<String> list, CallbackInfo ci) {
        for (String lang : list) {
            InputStream inputStream = TranslateHelper.setup(lang);
            this.load(inputStream);
            try {
                assert inputStream != null;
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
