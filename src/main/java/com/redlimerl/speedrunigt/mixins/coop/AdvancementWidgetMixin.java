package com.redlimerl.speedrunigt.mixins.coop;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.class_3326;
import net.minecraft.class_3358;
import net.minecraft.client.gui.AchievementNotification;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AchievementNotification.class)
public class AdvancementWidgetMixin {

    @Shadow @Final private class_3326 field_15987;

    @Redirect(method = "method_14525", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3358;method_15026()I"))
    public int onRender(class_3358 instance) {
        if (InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getStatus() != TimerStatus.NONE && InGameTimerUtils.COMPLETED_ADVANCEMENTS.contains(field_15987.method_14801().toString())) {
            return 0;
        }
        return instance.method_15026();
    }
}
