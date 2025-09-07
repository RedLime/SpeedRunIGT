package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {
    protected ClientWorldMixin(MutableWorldProperties mutableWorldProperties, RegistryKey<World> registryKey, RegistryKey<DimensionType> registryKey2, DimensionType dimensionType, Supplier<Profiler> profiler, boolean bl, boolean bl2, long l) {
        super(mutableWorldProperties, registryKey, registryKey2, dimensionType, profiler, bl, bl2, l);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void onTick(CallbackInfo ci) {
        InGameTimer.getInstance().tick();
    }

    @Unique
    private final int[][] heightmapAccumulator = new int[16 * 3][16 * 3];

    @Unique
    private final BlockPos.Mutable mutable = new BlockPos.Mutable();

    @Inject(method = "updateListeners", at = @At("TAIL"))
    public void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        RunCategory category = InGameTimer.getInstance().getCategory();
        if (category != RunCategories.MINE_A_CHUNK && category != RunCategories.MINE_A_CHUNK_SF) {
            return;
        }

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        for (int i = -1; i < 2; ++i) {
            for (int j = -1; j < 2; ++j) {
                // if all the chunks aren't loaded, it will break because the heightmap is reported as all 0's
                if (getChunk(chunkX + i, chunkZ + j, ChunkStatus.FULL, false) == null) {
                    return;
                }
            }
        }

        // checks for 16 x 16 squares that have blocks only at or below bedrock in the square from the chunks -1, -1 to 1, 1 relative to the chunk the block update was in
        // algorithm from https://stackoverflow.com/a/17790267
        for (int[] arr : heightmapAccumulator) {
            Arrays.fill(arr, 0);
        }

        boolean hasCeiling = this.getDimension().hasCeiling();
        int firstNonBedrockLayer = category == RunCategories.MINE_A_CHUNK ? 5 : (category == RunCategories.MINE_A_CHUNK_SF ? 1 : -1);
        assert firstNonBedrockLayer != -1;
        int lastNonBedrockLayer = this.getDimensionHeight() - 6;

        for (int x = 0; x < 16 * 3; ++x) {
            for (int z = 0; z < 16 * 3; ++z) {
                boolean columnClear = true;
                Chunk chunk = this.getChunk(chunkX + (x >> 4) - 1, chunkZ + (z >> 4) - 1, ChunkStatus.FULL, false);
                assert chunk != null; // already checked at the start
                if (!hasCeiling) {
                    // calculate chunk coordinates
                    columnClear = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(x & 15, z & 15) <= firstNonBedrockLayer;
                } else {
                    // check the column manually, use mutable for less churn
                    // calculate world coordinates
                    mutable.set((chunkX << 4) - 16 + x, 0, (chunkZ << 4) - 16 + z);
                    for (int y = firstNonBedrockLayer; y <= lastNonBedrockLayer; y++) {
                        mutable.setY(y);
                        if (!chunk.getBlockState(mutable).isAir()) {
                            columnClear = false;
                            break;
                        }
                    }
                }
                if (!columnClear) {
                    heightmapAccumulator[x][z] = 0;
                    continue;
                }
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
            }
        }
    }

    // for debugging purposes
    @Unique
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    private void printHeightmap(int chunkX, int chunkZ) {
        for (int x = 0; x < 16 * 3; ++x) {
            for (int z = 0; z < 16 * 3; ++z) {
                Chunk chunk = this.getChunk(chunkX + (x >> 4) - 1, chunkZ + (z >> 4) - 1);
                int height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(x & 15, z & 15);
                System.out.printf("%02d ", height);
            }
            System.out.println();
        }
        System.out.println();
    }
}
