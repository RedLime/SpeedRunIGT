package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {

    @Accessor("renderedEntitiesCount")
    int getRenderedEntityCount();

    @Invoker("getCompletedChunkCount")
    int invokeCompletedChunkCount();

}
