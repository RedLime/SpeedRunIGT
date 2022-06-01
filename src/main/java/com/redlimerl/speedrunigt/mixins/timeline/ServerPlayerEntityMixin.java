package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.class_3793;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.PlayerManager;
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

    @Inject(method = "method_1984", at = @At("HEAD"))
    public void onChangeDimension(ServerPlayerEntity serverPlayerEntity, class_3793 arg, CallbackInfo ci) {
        beforeWorld = serverPlayerEntity.getServerWorld();
        lastPortalPos = serverPlayerEntity.method_10787();
        InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = !InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY;
    }

    @Inject(method = "method_1984", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;method_1986(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER))
    public void onChangedDimension(ServerPlayerEntity serverPlayerEntity, class_3793 arg, CallbackInfo ci) {
        class_3793 oldDimension = beforeWorld.dimension.method_11789();
        class_3793 newDimension = serverPlayerEntity.world.dimension.method_11789();

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE) {
            if (oldDimension == class_3793.field_18955 && newDimension == class_3793.field_18954) {
                if (!timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY) InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(class_3793.field_18954, serverPlayerEntity.method_10787().add(0, 0, 0), lastPortalPos.add(0, 0, 0));
            }

            if (oldDimension == class_3793.field_18954 && newDimension == class_3793.field_18955) {
                if (InGameTimerUtils.isBlindTraveled(lastPortalPos)) {
                    InGameTimer.getInstance().tryInsertNewTimeline("nether_travel");
                }
                if (!timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY) InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(class_3793.field_18955, lastPortalPos.add(0, 0, 0), serverPlayerEntity.method_10787().add(0, 0, 0));
            }
        }
    }
}
