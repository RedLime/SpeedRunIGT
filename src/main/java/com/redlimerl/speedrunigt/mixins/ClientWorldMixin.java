package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
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

    public ClientWorldMixin(SaveHandler saveHandler, String string, Dimension dimension, LevelInfo levelInfo, Profiler profiler) {
        super(saveHandler, string, dimension, levelInfo, profiler);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void onTick(CallbackInfo ci) {
        InGameTimer.getInstance().tick();
    }

    @Override
    public boolean method_4721(int i, int j, int k, Block block, int l, int m) {
        boolean result = super.method_4721(i, j, k, block, l, m);

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getCategory() == RunCategories.MINE_A_CHUNK) {
            Chunk chunk = method_3680(i, k);
            for (int x = chunk.getChunkPos().getCenterX() - 8; x < chunk.getChunkPos().getCenterX() + 8; x++) {
                for (int y = getBedrockMaxHeight(); y < method_3771(); y++) {
                    for (int z = chunk.getChunkPos().getCenterZ() - 8; z < chunk.getChunkPos().getCenterZ() + 8; z++) {
                        Block targetBlock = method_3774(x, y, z);
                        if (targetBlock != Blocks.BEDROCK && targetBlock != Blocks.AIR) {
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
