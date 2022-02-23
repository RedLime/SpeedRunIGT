package com.redlimerl.speedrunigt.mixins.access;


import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextRenderer.class)
public interface TextRendererAccessor {

    @Accessor("fontStorage")
    FontStorage getFontStorage();
}