package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunPortalPos;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.OverworldDimension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {

    @Shadow @Final public Dimension dimension;

    @Shadow public boolean isClient;

    @Inject(method = "method_4721", at = @At("TAIL"))
    public void onUpdateBlockState(int x, int y, int z, Block block, int whatIsIt, int flags, CallbackInfoReturnable<Boolean> cir) {
        InGameTimer timer = InGameTimer.getInstance();
        if (!this.isClient && flags == 2 && block == Blocks.END_PORTAL && timer.getCategory() == RunCategories.ALL_PORTALS && timer.getCategory() == RunCategories.ALL_PORTALS && dimension instanceof OverworldDimension) {
            for (RunPortalPos runPortalPos : timer.getEndPortalPosList()) {
                if (runPortalPos.squaredDistanceTo(new Vec3i(x, y, z)) < 100) {
                    return;
                }
            }
            timer.getEndPortalPosList().add(new RunPortalPos(new Vec3i(x, y, z)));
            InGameTimerUtils.IS_KILLED_ENDER_DRAGON = false;
        }
    }
}
