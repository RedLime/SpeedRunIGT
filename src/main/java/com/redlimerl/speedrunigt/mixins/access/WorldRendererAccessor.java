package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {

    @Accessor("hiddenEntityCount")
    int getRegularEntityCount();

    @Invoker("method_12338")
    int invokeCompletedChunkCount();

    @Accessor("chunkBuilder")
    ChunkBuilder getChunkBuilder();

}