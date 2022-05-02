package com.redlimerl.speedrunigt.mixins.timeline;

import com.mojang.authlib.GameProfile;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    private ServerWorld beforeWorld = null;
    private Vec3d lastPortalPos = null;

    @Inject(method = "changeDimension", at = @At("HEAD"))
    public void onChangeDimension(DimensionType newDimension, CallbackInfoReturnable<Entity> cir) {
        beforeWorld = this.getServerWorld();
        lastPortalPos = this.getPos();
        InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = !InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY;
    }

    @Inject(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setWorld(Lnet/minecraft/world/World;)V", shift = At.Shift.AFTER))
    public void onChangedDimension(DimensionType d, CallbackInfoReturnable<Entity> cir) {
        DimensionType oldDimension = beforeWorld.dimension.getType();
        DimensionType newDimension = world.dimension.getType();

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE && !timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY) {
            if (oldDimension ==DimensionType.OVERWORLD && newDimension == DimensionType.THE_NETHER) {
                InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(DimensionType.THE_NETHER, this.getPos().add(0, 0, 0), lastPortalPos.add(0, 0, 0));
            }

            if (oldDimension == DimensionType.THE_NETHER && newDimension == DimensionType.OVERWORLD) {
                if (InGameTimerUtils.isBlindTraveled(lastPortalPos)) {
                    InGameTimer.getInstance().tryInsertNewTimeline("nether_travel");
                }
                InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(DimensionType.OVERWORLD, lastPortalPos.add(0, 0, 0), this.getPos().add(0, 0, 0));
            }
        }

        //All Portals
        SpeedRunIGT.debug("Current portals : " + timer.getEndPortalPosList().size());
        if (InGameTimerUtils.IS_KILLED_ENDER_DRAGON && timer.getCategory() == RunCategories.ALL_PORTALS && timer.getEndPortalPosList().size() == 128) {
            InGameTimer.complete();
        }
    }
}
