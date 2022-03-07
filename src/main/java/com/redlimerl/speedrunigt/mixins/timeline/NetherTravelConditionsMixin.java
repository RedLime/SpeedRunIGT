package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import net.minecraft.class_3218;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_3218.class_3220.class)
public class NetherTravelConditionsMixin {

    @Inject(method = "method_14361", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3164;method_14124(DDDDDD)Z", shift = At.Shift.BEFORE))
    public void onBlind(ServerWorld world, Vec3d enteredPos, double exitedPosX, double exitedPosY, double exitedPosZ, CallbackInfoReturnable<Boolean> cir) {
        if (InGameTimer.getInstance().getCategory() == RunCategories.ANY) {
            if (enteredPos.squaredDistanceTo(new Vec3d(exitedPosX, exitedPosY, exitedPosZ)) > 20) {
                InGameTimer.getInstance().tryInsertNewTimeline("nether_travel");
            }
        }
    }

}
