package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {

    @Accessor("hiddenEntityCount")
    int getRegularEntityCount();

    @Accessor("field_1896")
    int getCompletedChunkCount();

}