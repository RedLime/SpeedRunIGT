package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunPortalPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Inject(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setAsInsidePortal(Lnet/minecraft/world/level/block/Portal;Lnet/minecraft/core/BlockPos;)V", shift = At.Shift.BEFORE))
    public void onCollisionPlayer(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean bl, CallbackInfo ci) {
        if (entity instanceof Player && world instanceof ServerLevel) {
            InGameTimer timer = InGameTimer.getInstance();

            //All Portals
            if (entity.level().dimension() == Level.OVERWORLD) {
                boolean isNewPortal = true;
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
                    && timer.getCategory() == RunCategories.ALL_PORTALS && timer.getEndPortalPosList().size() >= 128) {
                InGameTimer.complete();
            }
        }
    }
}
