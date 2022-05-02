package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunPortalPos;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {

    @Shadow public abstract boolean isClient();

    @Shadow public abstract Dimension getDimension();

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", at = @At("TAIL"))
    public void onUpdateBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<Boolean> cir) {
        InGameTimer timer = InGameTimer.getInstance();
        if (!this.isClient() && flags == 2 && state.getBlock() == Blocks.END_PORTAL && timer.getCategory() == RunCategories.ALL_PORTALS && timer.getCategory() == RunCategories.ALL_PORTALS && getDimension().getType() == DimensionType.OVERWORLD) {
            for (RunPortalPos runPortalPos : timer.getEndPortalPosList()) {
                if (runPortalPos.squaredDistanceTo(pos) < 100) {
                    return;
                }
            }
            timer.getEndPortalPosList().add(new RunPortalPos(pos));
            InGameTimerUtils.IS_KILLED_ENDER_DRAGON = false;
        }
    }
}
