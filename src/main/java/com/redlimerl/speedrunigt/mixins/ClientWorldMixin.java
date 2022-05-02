package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunPortalPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.class_3793;
import net.minecraft.class_4070;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.SaveHandler;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * It is `ClientWorldMixin` class but ~1.13 have block updater is in World class. so... don't confuse it lol
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    protected ClientWorldMixin(SaveHandler saveHandler, @Nullable class_4070 arg, LevelProperties levelProperties, Dimension dimension, Profiler profiler, boolean bl) {
        super(saveHandler, arg, levelProperties, dimension, profiler, bl);
    }

    @Inject(at = @At("HEAD"), method = "method_16327")
    public void onTick(CallbackInfo ci) {
        InGameTimer.getInstance().tick();
    }

    @Override
    public void method_11481(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        super.method_11481(pos, oldState, newState, flags);

        InGameTimer timer = InGameTimer.getInstance();
        if (flags == 2 && newState.getBlock() == Blocks.END_PORTAL && timer.getCategory() == RunCategories.ALL_PORTALS && dimension.method_11789() == class_3793.field_18954) {
            for (RunPortalPos runPortalPos : timer.getEndPortalPosList()) {
                if (runPortalPos.squaredDistanceTo(pos) < 100) {
                    return;
                }
            }
            timer.getEndPortalPosList().add(new RunPortalPos(pos));

        }
        if (timer.getCategory() == RunCategories.MINE_A_CHUNK) {
            ChunkPos chunkPos = getChunk(pos).method_3920();
            for (int x = chunkPos.getActualX(); x < chunkPos.getOppositeX() + 1; x++) {
                for (int y = getBedrockMaxHeight(); y < getMaxBuildHeight(); y++) {
                    for (int z = chunkPos.getActualZ(); z < chunkPos.getOppositeZ() + 1; z++) {
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
        return 5;
    }
}
