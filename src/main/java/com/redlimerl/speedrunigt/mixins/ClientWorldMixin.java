package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.block.Block;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.logging.LogManager;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.SaveHandler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * It is `ClientWorldMixin` class but ~1.13 have block updater is in World class. so... don't confuse it lol
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    public ClientWorldMixin(SaveHandler saveHandler, String string, Dimension dimension, LevelInfo levelInfo, Profiler profiler, LogManager logger) {
        super(saveHandler, string, dimension, levelInfo, profiler, logger);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void onTick(CallbackInfo ci) {
        InGameTimer.getInstance().tick();
    }

    @Override
    public boolean method_4721(int x, int y, int z, int i, int j, int k) {
        boolean result = super.method_4721(x, y, z, i, j, k);

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getCategory() == RunCategories.MINE_A_CHUNK) {
            Chunk chunk = this.getChunk(i, k);
            for (int chunkX = chunk.getChunkPos().getCenterX() - 8; chunkX < chunk.getChunkPos().getCenterX() + 8; chunkX++) {
                for (int chunkY = getBedrockMaxHeight(); chunkY < method_3771(); chunkY++) {
                    for (int chunkZ = chunk.getChunkPos().getCenterZ() - 8; chunkZ < chunk.getChunkPos().getCenterZ() + 8; chunkZ++) {
                        int targetBlock = getBlock(chunkX, chunkY, chunkZ);
                        if (targetBlock != Block.BEDROCK.id && targetBlock != 0) {
                            return result;
                        }
                    }
                }
            }
            InGameTimer.complete();
        }
        return result;
    }

    private int getBedrockMaxHeight() {
        return 5;
    }
}
