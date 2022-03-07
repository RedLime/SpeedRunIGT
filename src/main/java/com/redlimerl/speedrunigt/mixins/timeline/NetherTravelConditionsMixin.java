package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import net.minecraft.advancement.criterion.TravelCriterion;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TravelCriterion.Conditions.class)
public class NetherTravelConditionsMixin {

    @Inject(method = "matches", at = @At(value = "INVOKE", target = "Lnet/minecraft/predicate/entity/DistancePredicate;test(DDDDDD)Z", shift = At.Shift.BEFORE))
    public void onBlind(ServerWorld world, Vec3d startPos, Vec3d endPos, CallbackInfoReturnable<Boolean> cir) {
        if (InGameTimer.getInstance().getCategory() == RunCategories.ANY) {
            if (startPos.squaredDistanceTo(endPos) > 20) {
                InGameTimer.getInstance().tryInsertNewTimeline("nether_travel");
            }
        }
    }

}
