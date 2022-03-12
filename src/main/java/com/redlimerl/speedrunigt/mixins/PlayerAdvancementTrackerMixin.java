package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.advancement.AdvancementFile;
import net.minecraft.advancement.SimpleAdvancement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(AdvancementFile.class)
public class PlayerAdvancementTrackerMixin {

    @ModifyArgs(method = "method_14929", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/Criterion$class_3353;<init>(Lnet/minecraft/advancement/criterion/CriterionInstance;Lnet/minecraft/advancement/SimpleAdvancement;Ljava/lang/String;)V"))
    private void getCriteria(Args args) {
        SimpleAdvancement advancement = args.get(1);
        String criteriaKey = args.get(2);
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) {
            InGameTimer.getInstance().tryInsertNewAdvancement(advancement.getIdentifier().toString(), criteriaKey);
        }
    }
}
