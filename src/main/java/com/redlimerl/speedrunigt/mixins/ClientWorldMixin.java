package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void onTick(CallbackInfo ci) {
        InGameTimer.getInstance().tick();
    }

    @Inject(method = "updateListeners", at = @At("TAIL"))
    public void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getCategory() == RunCategories.MINE_A_CHUNK) {
            ChunkPos chunkPos = getChunk(pos).getPos();
            for (int x = chunkPos.getStartX(); x < chunkPos.getEndX() + 1; x++) {
                for (int y = getBedrockMaxHeight(); y < getHeight(); y++) {
                    for (int z = chunkPos.getStartZ(); z < chunkPos.getEndZ() + 1; z++) {
                        Block block = getBlockState(new BlockPos(x, y, z)).getBlock();
                        if (block != Blocks.BEDROCK && block != Blocks.AIR) {
                            return;
                        }
                    }
                }
            }
            InGameTimer.complete();
        }
    }

    private int getBedrockMaxHeight() {
        return getBottomY() + 5;
    }
}
