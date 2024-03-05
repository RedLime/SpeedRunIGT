package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunPortalPos;
import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.logging.LogManager;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.SaveHandler;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

    public ServerWorldMixin(SaveHandler saveHandler, String string, Dimension dimension, LevelInfo levelInfo, Profiler profiler, LogManager logManager) {
        super(saveHandler, string, dimension, levelInfo, profiler, logManager);
    }

    @Override
    public boolean method_4721(int i, int j, int k, int block, int l, int m) {
        boolean result = super.method_4721(i, j, k, block, l, m);

        InGameTimer timer = InGameTimer.getInstance();
        if (!this.isClient && m == 2 && block == Block.END_PORTAL.id && timer.getCategory() == RunCategories.ALL_PORTALS && dimension instanceof OverworldDimension) {
            for (RunPortalPos runPortalPos : timer.getEndPortalPosList()) {
                if (runPortalPos.squaredDistanceTo(new Vec3i(i, j, k)) < 100) {
                    return result;
                }
            }
            SpeedRunIGT.debug("Detected serverworld end portal");
            timer.getEndPortalPosList().add(new RunPortalPos(new Vec3i(i, j, k)));
            timer.tryInsertNewTimeline("portal_no_"+timer.getEndPortalPosList().size());
            InGameTimerUtils.IS_KILLED_ENDER_DRAGON = false;
        }

        return result;
    }
}
