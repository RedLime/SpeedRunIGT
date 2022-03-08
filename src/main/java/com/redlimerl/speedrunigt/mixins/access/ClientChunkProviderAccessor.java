package com.redlimerl.speedrunigt.mixins.access;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ClientChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkProvider.class)
public interface ClientChunkProviderAccessor {
    @Accessor("chunkMap")
    Long2ObjectMap<Chunk> getChunkMap();
}
