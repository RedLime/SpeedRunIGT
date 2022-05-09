package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunPortalPos;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.class_3793;
import net.minecraft.class_4070;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.SaveHandler;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

    protected ServerWorldMixin(SaveHandler saveHandler, @Nullable class_4070 arg, LevelProperties levelProperties, Dimension dimension, Profiler profiler, boolean bl) {
        super(saveHandler, arg, levelProperties, dimension, profiler, bl);
    }

    @Override
    public boolean method_8506(BlockPos pos, BlockState state, int flags) {
        boolean result = super.method_8506(pos, state, flags);

        InGameTimer timer = InGameTimer.getInstance();
        if (!this.method_16390() && flags == 2 && state.getBlock() == Blocks.END_PORTAL && timer.getCategory() == RunCategories.ALL_PORTALS && timer.getCategory() == RunCategories.ALL_PORTALS && dimension.method_11789() == class_3793.field_18954) {
            for (RunPortalPos runPortalPos : timer.getEndPortalPosList()) {
                if (runPortalPos.squaredDistanceTo(pos) < 100) {
                    return result;
                }
            }
            SpeedRunIGT.debug("Detected serverworld end portal");
            timer.getEndPortalPosList().add(new RunPortalPos(pos));
            timer.tryInsertNewTimeline("portal_no_"+timer.getEndPortalPosList().size());
            InGameTimerUtils.IS_KILLED_ENDER_DRAGON = false;
        }

        return result;
    }
}
