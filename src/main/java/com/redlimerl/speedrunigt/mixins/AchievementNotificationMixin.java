package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Lists;
import com.redlimerl.speedrunigt.instance.GameInstance;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.condition.AdvancementCategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerAchieveAdvancementPacket;
import net.minecraft.advancement.Achievement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AchievementNotification;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AchievementNotification.class)
public abstract class AchievementNotificationMixin {

    @Inject(method = "method_1092", at = @At("HEAD"))
    public void onComplete(Achievement achieved, CallbackInfo ci){
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.NONE) return;

        // Events system
        GameInstance.getInstance().callEvents("achievement", factory -> achieved.getStringId().substring("achievement.".length()).equals(factory.getDataValue("achievement")));

        // For Timeline
        timer.tryInsertNewAdvancement(achieved.getStringId(), null, true);
        if (timer.isCoop() && (timer.getCategory() == RunCategories.ALL_ACHIEVEMENTS || timer.getCategory() == RunCategories.HALF || timer.getCategory() == RunCategories.POGLOOT_QUATER))
            TimerPacketUtils.sendClient2ServerPacket(Minecraft.getMinecraft(), new TimerAchieveAdvancementPacket(achieved.getStringId()));

        // Custom Json category
        if (timer.getCategory().getConditionJson() != null) {
            for (CategoryCondition.Condition<?> condition : timer.getCustomCondition().map(CategoryCondition::getConditionList).orElse(Lists.newArrayList())) {
                if (condition instanceof AdvancementCategoryCondition) {
                    timer.updateCondition((AdvancementCategoryCondition) condition, achieved);
                }
            }
            timer.checkConditions();
        }

        int maxCount = InGameTimer.getInstance().getMoreData(7441);

        //All Advancements
        if (timer.getCategory() == RunCategories.ALL_ACHIEVEMENTS) {
            if (getCompleteAdvancementsCount() >= maxCount) InGameTimer.complete();
        }

        //Half%
        if (timer.getCategory() == RunCategories.HALF) {
            if (getCompleteAdvancementsCount() >= MathHelper.ceil(maxCount / 2.0f)) InGameTimer.complete();
        }

        //Half%
        if (timer.getCategory() == RunCategories.POGLOOT_QUATER) {
            if (getCompleteAdvancementsCount() >= MathHelper.ceil(maxCount / 4.0f)) InGameTimer.complete();
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
