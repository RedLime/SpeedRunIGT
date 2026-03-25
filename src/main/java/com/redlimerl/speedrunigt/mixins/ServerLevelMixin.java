package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.running.RunPortalPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {

    protected ServerLevelMixin(WritableLevelData properties, ResourceKey<Level> registryRef, RegistryAccess registryManager, Holder<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags) {
        boolean result = super.setBlock(pos, state, flags);

        InGameTimer timer = InGameTimer.getInstance();
        if (!this.isClientSide() && flags == 2 && state.getBlock() == Blocks.END_PORTAL && Objects.equals(dimension().identifier().toString(), BuiltinDimensionTypes.OVERWORLD.toString())) {
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
