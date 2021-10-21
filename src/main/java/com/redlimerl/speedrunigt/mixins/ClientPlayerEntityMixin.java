package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends LivingEntity {

    protected ClientPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
            at = @At("TAIL"))
    private void onMove(MovementType movementType, Vec3d vec3d, CallbackInfo ci) {
        @NotNull InGameTimer timer = InGameTimer.INSTANCE;
        if (timer.getStatus() == TimerStatus.IDLE && timer.isPause() && (vec3d.x != 0 || vec3d.z != 0 || this.jumping)) {
            timer.setPause(false);
        }
    }
}
