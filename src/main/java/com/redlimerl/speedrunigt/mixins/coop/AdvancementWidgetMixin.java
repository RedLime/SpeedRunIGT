package com.redlimerl.speedrunigt.mixins.coop;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.advancement.Advancement;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AdvancementWidget.class)
public class AdvancementWidgetMixin {

    @Shadow @Final private Advancement advancement;

    @Redirect(method = "renderWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementObtainedStatus;getSpriteIndex()I"))
    public int onRender(AdvancementObtainedStatus instance) {
        if (InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getStatus() != TimerStatus.NONE && InGameTimerUtils.COMPLETED_ADVANCEMENTS.contains(advancement.getId().toString())) {
            return 0;
        }
        return instance.getSpriteIndex();
    }
}
