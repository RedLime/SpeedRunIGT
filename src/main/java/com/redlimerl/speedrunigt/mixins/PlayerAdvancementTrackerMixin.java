package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {

    @ModifyArgs(method = "endTrackingCompleted", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/Criterion$ConditionsContainer;<init>(Lnet/minecraft/advancement/criterion/CriterionConditions;Lnet/minecraft/advancement/Advancement;Ljava/lang/String;)V"))
    private void getCriteria(Args args) {
        Advancement advancement = args.get(1);
        String criteriaKey = args.get(2);
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE && advancement.getDisplay() != null) {
            InGameTimer.getInstance().tryInsertNewAdvancement(advancement.getId().toString(), criteriaKey);
        }
    }
}
