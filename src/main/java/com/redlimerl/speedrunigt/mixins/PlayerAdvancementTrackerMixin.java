package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.class_3326;
import net.minecraft.class_3347;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(class_3347.class)
public class PlayerAdvancementTrackerMixin {

    @ModifyArgs(method = "method_14929", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3352$class_3353;<init>(Lnet/minecraft/class_3354;Lnet/minecraft/class_3326;Ljava/lang/String;)V"))
    private void getCriteria(Args args) {
        class_3326 advancement = args.get(1);
        String criteriaKey = args.get(2);
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) {
            InGameTimer.getInstance().tryInsertNewAdvancement(advancement.method_14801().toString(), criteriaKey);
        }
    }
}
