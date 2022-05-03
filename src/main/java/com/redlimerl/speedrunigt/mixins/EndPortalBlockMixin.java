package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Inject(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;method_3197(I)Lnet/minecraft/entity/Entity;", shift = At.Shift.AFTER))
    public void onCollisionPlayer(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (entity instanceof PlayerEntity && world instanceof ServerWorld) {
            InGameTimer timer = InGameTimer.getInstance();

            //All Portals
            SpeedRunIGT.debug("Current portals : " + timer.getEndPortalPosList().size());
            if (InGameTimerUtils.IS_KILLED_ENDER_DRAGON && timer.getStatus() != TimerStatus.NONE
                    && timer.getCategory() == RunCategories.ALL_PORTALS && timer.getEndPortalPosList().size() >= 128) {
                InGameTimer.complete();
            }
        }
    }
}
