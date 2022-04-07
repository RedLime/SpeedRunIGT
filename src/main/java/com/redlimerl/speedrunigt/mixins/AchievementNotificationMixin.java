package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.condition.AdvancementCategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerAchieveAdvancementPacket;
import net.minecraft.achievement.Achievement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.AchievementNotification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AchievementNotification.class)
public abstract class AchievementNotificationMixin {

    @Inject(method = "display", at = @At("HEAD"))
    public void onComplete(Achievement achieved, CallbackInfo ci){
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.NONE) return;

        // For Timeline
        timer.tryInsertNewAdvancement(achieved.name, null, true);
        if (timer.isCoop() && (timer.getCategory() == RunCategories.ALL_ACHIEVEMENTS || timer.getCategory() == RunCategories.HALF || timer.getCategory() == RunCategories.POGLOOT_QUATER))
            TimerPacketUtils.sendClient2ServerPacket(MinecraftClient.getInstance(), new TimerAchieveAdvancementPacket(achieved.name));

        // Custom Json category
        if (timer.getCategory().getConditionJson() != null) {
            for (CategoryCondition.Condition<?> condition : timer.getCustomCondition().getConditionList()) {
                if (condition instanceof AdvancementCategoryCondition) {
                    timer.updateCondition((AdvancementCategoryCondition) condition, achieved);
                }
            }
            timer.checkConditions();
        }

        //All Advancements
        if (timer.getCategory() == RunCategories.ALL_ACHIEVEMENTS) {
            if (getCompleteAdvancementsCount() >= 34) InGameTimer.complete();
        }

        //Half%
        if (timer.getCategory() == RunCategories.HALF) {
            if (getCompleteAdvancementsCount() >= 17) InGameTimer.complete();
        }

        //Half%
        if (timer.getCategory() == RunCategories.POGLOOT_QUATER) {
            if (getCompleteAdvancementsCount() >= 9) InGameTimer.complete();
        }
    }

    private int getCompleteAdvancementsCount() {
        int count = 0;
        for (Map.Entry<String, TimerAdvancementTracker.AdvancementTrack> track : InGameTimer.getInstance().getAdvancementsTracker().getAdvancements().entrySet()) {
            if (track.getValue().isAdvancement() && track.getValue().isComplete()) count++;
        }
        return count;
    }
}
