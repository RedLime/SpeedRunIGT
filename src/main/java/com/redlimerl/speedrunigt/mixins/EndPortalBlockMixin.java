package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunPortalPos;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.dimension.OverworldDimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Inject(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;teleportToDimension(I)V", shift = At.Shift.BEFORE))
    public void onCollisionPlayer(World world, int x, int y, int z, Entity entity, CallbackInfo ci) {
        if (entity instanceof PlayerEntity && world instanceof ServerWorld) {
            InGameTimer timer = InGameTimer.getInstance();

            //All Portals
            if (entity.world.dimension instanceof OverworldDimension) {
                boolean isNewPortal = true;
                Vec3i pos = new Vec3i(x, y, z);
                for (RunPortalPos runPortalPos : timer.getEndPortalPosList()) {
                    if (runPortalPos.squaredDistanceTo(pos) < 100) {
                        isNewPortal = false;
                        break;
                    }
                }
                if (isNewPortal) {
                    timer.getEndPortalPosList().add(new RunPortalPos(pos));
                    timer.tryInsertNewTimeline("portal_no_"+timer.getEndPortalPosList().size());
                    InGameTimerUtils.IS_KILLED_ENDER_DRAGON = false;
                }
            }

            SpeedRunIGT.debug("Current portals : " + timer.getEndPortalPosList().size());
            if (InGameTimerUtils.IS_KILLED_ENDER_DRAGON && timer.getStatus() != TimerStatus.NONE
                    && timer.getCategory() == RunCategories.ALL_PORTALS && timer.getEndPortalPosList().size() >= 3) {
                InGameTimer.complete();
            }
        }
    }
}
