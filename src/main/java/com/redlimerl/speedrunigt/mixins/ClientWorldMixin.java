package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {
    @Shadow
    public abstract ClientWorld.Properties getLevelProperties();

    @Shadow
    @Final
    private MinecraftClient client;

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }


    @Inject(at = @At("HEAD"), method = "tick")
    public void onTick(CallbackInfo ci) {
        InGameTimer.getInstance().tick();
    }

    @Inject(method = "updateListeners", at = @At("TAIL"))
    public void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        // TODO: doesn't support nether or overworld caves
        if (this.getDimension().hasCeiling()) {
            return;
        }

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getCategory() == RunCategories.MINE_A_CHUNK) {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            for (int i = -1; i < 2; ++i) {
                for (int j = -1; j < 2; ++j) {
                    // if all the chunks aren't loaded (and chunks are given as EmptyChunks), it will break because the heightmap is reported as all 0's
                    if (getChunk(chunkX + i, chunkZ + j) instanceof EmptyChunk) {
                        return;
                    }
                }
            }

            // checks for 16 x 16 squares that have blocks only at or below bedrock in the square from the chunks -1, -1 to 1, 1 relative to the chunk the block update was in
            // algorithm from https://stackoverflow.com/a/17790267
            int[][] heightmapAccumulator = new int[16 * 3][16 * 3];

            for (int x = 0; x < 16 * 3; ++x) {
                for (int z = 0; z < 16 * 3; ++z) {
                    // convert the 0 to 47 x and z counters in to -1 to 1 chunk offsets
                    Chunk chunk = this.getChunk(chunkX + (x >> 4) - 1, chunkZ + (z >> 4) - 1);
                    int height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(mod(x, 16), mod(z, 16));
                    if (height <= getBedrockMaxHeight()) {
                        if (x == 0 || z == 0) {
                            // special case for first row and column, no previous work to check
                            heightmapAccumulator[x][z] = 1;
                            continue;
                        }
                        //  calculate the max value the next square is allowed to be using the bounds of the previous adjacent ones
                        int currentSquareLevel = Math.min(Math.min(heightmapAccumulator[x - 1][z], heightmapAccumulator[x][z - 1]), heightmapAccumulator[x - 1][z - 1]) + 1;
                        // if we hit 16 on the square level, we've found a large enough area
                        if (currentSquareLevel == 16) {
                            InGameTimer.complete();
                            return;
                        }
                        // otherwise, just assign the value to it's place in the matrix
                        heightmapAccumulator[x][z] = currentSquareLevel;
                    } else {
                        heightmapAccumulator[x][z] = 0;
                    }
                }
            }
        }
    }

    // handles negative numbers correctly, like python
    @Unique
    private static int mod(int divisor, int dividend) {
        return ((divisor % dividend) + dividend) % dividend;
    }

    @Unique
    private int getBedrockMaxHeight() {
        if (this.client.isIntegratedServerRunning() && this.client.getServer().getWorld((this.getRegistryKey())).getChunkManager().getChunkGenerator() instanceof FlatChunkGenerator) {
            return this.getBottomY() + 1;
        }
        return this.getBottomY() + 5;
    }

    // for debugging purposes
    @Unique
    private void printHeightmapAccumulator(int[][] heightmap) {
        for (int[] row : heightmap) {
            for (int height : row) {
                System.out.printf("%02d ", height);
            }
            System.out.println();
        }
        System.out.println();
    }

    @Unique
    private void printHeightmap(int chunkX, int chunkZ) {
        for (int x = 0; x < 16 * 3; ++x) {
            for (int z = 0; z < 16 * 3; ++z) {
                Chunk chunk = this.getChunk(chunkX + (x >> 4) - 1, chunkZ + (z >> 4) - 1);
                int height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(mod(x, 16), mod(z, 16));
                System.out.printf("%02d ", height);
            }
            System.out.println();
        }
        System.out.println();
    }
}
