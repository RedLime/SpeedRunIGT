package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(FontManager.class)
public interface FontManagerAccessor {

    @Accessor("fontSets")
    Map<Identifier, FontSet> getFontSets();

    @Accessor("textureManager")
    TextureManager getTextureManager();
}
