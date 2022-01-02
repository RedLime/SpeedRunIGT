package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.RenderableGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FontStorage.class)
public interface FontStorageAccessor {

    @Invoker("getRenderableGlyph")
    RenderableGlyph invokeRenderableGlyph(int c);

}
