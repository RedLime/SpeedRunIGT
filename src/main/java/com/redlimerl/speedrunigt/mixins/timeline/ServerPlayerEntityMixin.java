package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.dimension.TheNetherDimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class ServerPlayerEntityMixin {

    private ServerWorld beforeWorld = null;
    private Vec3d lastPortalPos = null;

    @Inject(method = "teleportToDimension", at = @At("HEAD"))
    public void onChangeDimension(ServerPlayerEntity player, int dimension, CallbackInfo ci) {
        beforeWorld = player.getServerWorld();
        lastPortalPos = Vec3d.method_6609(player.x, player.y, player.z);
        InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = !InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY;
    }

    @Inject(method = "teleportToDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;method_1986(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER))
    public void onChangedDimension(ServerPlayerEntity player, int dimension, CallbackInfo ci) {
        Dimension oldDimension = beforeWorld.dimension;
        Dimension newDimension = player.world.dimension;

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE && !timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY) {
            if (oldDimension instanceof OverworldDimension && newDimension instanceof TheNetherDimension) {
                InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(newDimension, Vec3d.method_6609(player.x, player.y, player.z), lastPortalPos.add(0, 0, 0));
            }

            if (oldDimension instanceof TheNetherDimension && newDimension instanceof OverworldDimension) {
                if (InGameTimerUtils.isBlindTraveled(lastPortalPos)) {
                    InGameTimer.getInstance().tryInsertNewTimeline("nether_travel");
                }
                InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(newDimension, lastPortalPos.add(0, 0, 0), Vec3d.method_6609(player.x, player.y, player.z));
            }
        }

        //All Portals
        SpeedRunIGT.debug("Current portals : " + timer.getEndPortalPosList().size());
        if (InGameTimerUtils.IS_KILLED_ENDER_DRAGON && timer.getCategory() == RunCategories.ALL_PORTALS && timer.getEndPortalPosList().size() == 3) {
            InGameTimer.complete();
        }
    }
}
