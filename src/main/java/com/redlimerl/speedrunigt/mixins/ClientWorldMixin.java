package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.class_0_378;
import net.minecraft.class_30;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.ProfilerSystem;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * It is `ClientWorldMixin` class but ~1.13 have block updater is in World class. so... don't confuse it lol
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    protected ClientWorldMixin(class_30 handler, LevelProperties properties, Dimension dim, ProfilerSystem profiler, boolean isClient) {
        super(handler, properties, dim, profiler, isClient);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void onTick(CallbackInfo ci) {
        InGameTimer.getInstance().tick();
    }

    @Override
    public void method_8413(BlockPos pos, class_0_378 oldState, class_0_378 newState, int flags) {
        super.method_8413(pos, oldState, newState, flags);

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getCategory() == RunCategories.MINE_A_CHUNK) {
            ChunkPos chunkPos = getWorldChunk(pos).method_12004();
            for (int x = chunkPos.getStartX(); x < chunkPos.getEndX() + 1; x++) {
                for (int y = getBedrockMaxHeight(); y < method_0_260(); y++) {
                    for (int z = chunkPos.getStartZ(); z < chunkPos.getEndZ() + 1; z++) {
                        class_0_378 blockState = method_0_484(new BlockPos(x, y, z));
                        Block block = blockState.method_0_1225();
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
        return 5;
    }
}
