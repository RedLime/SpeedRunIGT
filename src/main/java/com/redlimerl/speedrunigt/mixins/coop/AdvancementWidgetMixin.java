package com.redlimerl.speedrunigt.mixins.coop;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.advancement.AdvancementType;
import net.minecraft.advancement.SimpleAdvancement;
import net.minecraft.client.gui.AchievementNotification;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AchievementNotification.class)
public class AdvancementWidgetMixin {

    @Shadow @Final private SimpleAdvancement advancement;

    @Redirect(method = "method_14525", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementType;getTextureOffest()I"))
    public int onRender(AdvancementType instance) {
        if (InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getStatus() != TimerStatus.NONE && InGameTimerUtils.COMPLETED_ADVANCEMENTS.contains(advancement.getIdentifier().toString())) {
            return 0;
        }
        return instance.getTextureOffest();
    }
}
