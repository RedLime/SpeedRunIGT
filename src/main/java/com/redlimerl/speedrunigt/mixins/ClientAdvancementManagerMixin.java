package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.condition.AdvancementCategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerAchieveAdvancementPacket;
import net.minecraft.*;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Mixin(ClientAdvancementManager.class)
public abstract class ClientAdvancementManagerMixin {

    @Shadow @Final private AdvancementManager field_16128;

    @Shadow @Final private MinecraftClient field_16127;

    @Shadow @Final private Map<Advancement, AdvancementProgress> field_16129;

    @ModifyVariable(method = "onProgressUpdate", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;"))
    public Map.Entry<Identifier, AdvancementProgress> advancement(Map.Entry<Identifier, AdvancementProgress> entry) {
        InGameTimer timer = InGameTimer.getInstance();

        Advancement advancement = this.field_16128.get(entry.getKey());
        AdvancementProgress advancementProgress = entry.getValue();
        assert advancement != null;
        advancementProgress.init(advancement.getCriteria(), advancement.getRequirements());

        if (advancementProgress.isDone() && timer.getStatus() != TimerStatus.NONE) {

            // For Timelines
            if (Objects.equals(advancement.getId().getPath(), "story/follow_ender_eye")) {
                timer.tryInsertNewTimeline("enter_stronghold");
            } else if (Objects.equals(advancement.getId().getPath(), "nether/find_fortress")) {
                timer.tryInsertNewTimeline("enter_fortress");
            }
            timer.tryInsertNewAdvancement(advancement.getId().toString(), null, advancement.getDisplay() != null);
            if (timer.isCoop() && advancement.getDisplay() != null) {
                TimerPacketUtils.sendClient2ServerPacket(field_16127, new TimerAchieveAdvancementPacket(advancement));
            }

            // Custom Json category
            if (timer.getCategory().getConditionJson() != null) {
                for (CategoryCondition.Condition<?> condition : timer.getCustomCondition().map(CategoryCondition::getConditionList).orElse(Lists.newArrayList())) {
                    if (condition instanceof AdvancementCategoryCondition) {
                        timer.updateCondition((AdvancementCategoryCondition) condition, advancement);
                    }
                }
                timer.checkConditions();
            }

            //How Did We Get Here
            if (timer.getCategory() == RunCategories.HOW_DID_WE_GET_HERE && Objects.equals(advancement.getId().toString(), new Identifier("nether/all_effects").toString())) {
                InGameTimer.complete();
            }
        }
        return entry;
    }

    @Inject(at = @At("RETURN"), method = "onProgressUpdate")
    public void onComplete(AdvancementUpdateS2CPacket arg, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        int maxCount = timer.getMoreData(7441) == 0 ? 80 : timer.getMoreData(7441);

        //All Advancements
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.ALL_ADVANCEMENTS) {
            if (getCompleteAdvancementsCount() >= maxCount) InGameTimer.complete();
        }

        //Half%
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.HALF) {
            if (getCompleteAdvancementsCount() >= MathHelper.ceil(maxCount / 2.0f)) InGameTimer.complete();
        }

        //(PogLoot) Quater
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.POGLOOT_QUATER) {
            if (getCompleteAdvancementsCount() >= MathHelper.ceil(maxCount / 4.0f)) InGameTimer.complete();
        }
    }

    private int getCompleteAdvancementsCount() {
        Set<String> completedAdvancements = Sets.newHashSet();
        for (Map.Entry<String, TimerAdvancementTracker.AdvancementTrack> track : InGameTimer.getInstance().getAdvancementsTracker().getAdvancements().entrySet()) {
            if (track.getValue().isAdvancement() && track.getValue().isComplete()) completedAdvancements.add(track.getKey());
        }
        for (Advancement advancement : this.field_16128.method_712()) {
            if (this.field_16129.containsKey(advancement) && advancement.getDisplay() != null) {
                AdvancementProgress advancementProgress = this.field_16129.get(advancement);

                advancementProgress.init(advancement.getCriteria(), advancement.getRequirements());
                String advancementID = advancement.getId().toString();
                if (advancementProgress.isDone() && completedAdvancements.contains(advancementID)) {
                    completedAdvancements.add(advancementID);
                    InGameTimer.getInstance().tryInsertNewAdvancement(advancementID, null, true);
                }
            }
        }
        return completedAdvancements.size();
    }
}
