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
import net.minecraft.advancement.*;
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

    @Shadow @Final private AdvancementManager manager;

    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract AdvancementManager getManager();

    @Shadow @Final private Map<Advancement, AdvancementProgress> advancementProgresses;

    @ModifyVariable(method = "onAdvancements", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;"))
    public Map.Entry<Identifier, AdvancementProgress> advancement(Map.Entry<Identifier, AdvancementProgress> entry) {
        InGameTimer timer = InGameTimer.getInstance();
        
        PlacedAdvancement advancement = this.manager.get(entry.getKey());
        AdvancementProgress advancementProgress = entry.getValue();
        assert advancement != null;
        advancementProgress.init(advancement.getAdvancement().requirements());

        if (advancementProgress.isDone() && timer.getStatus() != TimerStatus.NONE) {

            // For Timelines
            if (Objects.equals(advancement.getAdvancementEntry().id().getPath(), "story/follow_ender_eye")) {
                timer.tryInsertNewTimeline("enter_stronghold");
            } else if (Objects.equals(advancement.getAdvancementEntry().id().getPath(), "nether/find_bastion")) {
                timer.tryInsertNewTimeline("enter_bastion");
            } else if (Objects.equals(advancement.getAdvancementEntry().id().getPath(), "nether/find_fortress")) {
                timer.tryInsertNewTimeline("enter_fortress");
            }

            timer.tryInsertNewAdvancement(advancement.getAdvancementEntry().id().toString(), null, advancement.getAdvancement().display().isPresent());
            if (timer.isCoop() && advancement.getAdvancement().display().isPresent()) {
                TimerPacketUtils.sendClient2ServerPacket(client, new TimerAchieveAdvancementPacket(advancement.getAdvancementEntry()));
            }

            // Custom Json category
            if (timer.getCategory().getConditionJson() != null) {
                for (CategoryCondition.Condition<?> condition : timer.getCustomCondition().map(CategoryCondition::getConditionList).orElse(Lists.newArrayList())) {
                    if (condition instanceof AdvancementCategoryCondition) {
                        timer.updateCondition((AdvancementCategoryCondition) condition, advancement.getAdvancementEntry());
                    }
                }
                timer.checkConditions();
            }

            //How Did We Get Here
            if (timer.getCategory() == RunCategories.HOW_DID_WE_GET_HERE && Objects.equals(advancement.getAdvancementEntry().id().toString(), new Identifier("nether/all_effects").toString())) {
                InGameTimer.complete();
            }

            //Hero of Village
            if (timer.getCategory() == RunCategories.HERO_OF_VILLAGE && Objects.equals(advancement.getAdvancementEntry().id().toString(), new Identifier("adventure/hero_of_the_village").toString())) {
                InGameTimer.complete();
            }

            //Arbalistic
            if (timer.getCategory() == RunCategories.ARBALISTIC && Objects.equals(advancement.getAdvancementEntry().id().toString(), new Identifier("adventure/arbalistic").toString())) {
                InGameTimer.complete();
            }

            //Cover Me In Debris
            if (timer.getCategory() == RunCategories.COVER_ME_IN_DEBRIS && Objects.equals(advancement.getAdvancementEntry().id().toString(), new Identifier("nether/netherite_armor").toString())) {
                InGameTimer.complete();
            }
        }
        return entry;
    }

    @Inject(at = @At("RETURN"), method = "onAdvancements")
    public void onComplete(AdvancementUpdateS2CPacket packet, CallbackInfo ci) {
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
        for (PlacedAdvancement advancement : this.getManager().getAdvancements()) {
            if (this.advancementProgresses.containsKey(advancement.getAdvancement()) && advancement.getAdvancement().display().isPresent()) {
                AdvancementProgress advancementProgress = this.advancementProgresses.get(advancement.getAdvancement());

                advancementProgress.init(advancement.getAdvancement().requirements());
                String advancementID = advancement.getAdvancementEntry().id().toString();
                if (advancementProgress.isDone() && completedAdvancements.contains(advancementID)) {
                    completedAdvancements.add(advancementID);
                    InGameTimer.getInstance().tryInsertNewAdvancement(advancementID, null, true);
                }
            }
        }
        return completedAdvancements.size();
    }
}
