package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.class_2750;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
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
        lastPortalPos = player.getPos();
        InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = !InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY;
    }

    @Inject(method = "teleportToDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;method_1986(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER))
    public void onChangedDimension(ServerPlayerEntity player, int dimension, CallbackInfo ci) {
        class_2750 oldDimension = beforeWorld.dimension.method_11789();
        class_2750 newDimension = player.world.dimension.method_11789();

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE && !timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY) {
            if (oldDimension == class_2750.field_12920 && newDimension == class_2750.field_12921) {
                InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(class_2750.field_12921, player.getPos().add(0, 0, 0), lastPortalPos.add(0, 0, 0));
            }

            if (oldDimension == class_2750.field_12921 && newDimension == class_2750.field_12920) {
                if (InGameTimerUtils.isBlindTraveled(lastPortalPos)) {
                    InGameTimer.getInstance().tryInsertNewTimeline("nether_travel");
                }
                InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(class_2750.field_12920, lastPortalPos.add(0, 0, 0), player.getPos().add(0, 0, 0));
            }
        }
    }
}
