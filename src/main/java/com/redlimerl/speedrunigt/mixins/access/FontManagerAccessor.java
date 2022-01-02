package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(FontManager.class)
public interface FontManagerAccessor {

    @Accessor("fontStorages")
    Map<Identifier, FontStorage> getFontStorages();

    @Accessor("textureManager")
    TextureManager getTextureManager();
}
