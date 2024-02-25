package com.redlimerl.speedrunigt.mixins.access;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkManager.class)
public interface ClientChunkProviderAccessor {
    @Accessor("chunkMap")
    Long2ObjectMap<WorldChunk> getChunkMap();
}
