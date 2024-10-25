package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ServerChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerChunkProvider.class)
public interface ServerChunkProviderAccessor {

    @Accessor("chunkGenerator")
    ChunkProvider getChunkGenerator();

}
