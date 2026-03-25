package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.redlimerl.speedrunigt.instance.GameInstance;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.condition.AdvancementCategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerAchieveAdvancementPacket;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Mixin(ClientAdvancements.class)
public abstract class ClientAdvancementsMixin {
    @Shadow @Final private AdvancementTree tree;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private Map<Advancement, AdvancementProgress> progress;
    @Shadow public abstract AdvancementTree getTree();

    @ModifyVariable(method = "update", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;"))
    public Map.Entry<Identifier, AdvancementProgress> advancement(Map.Entry<Identifier, AdvancementProgress> entry) {
        InGameTimer timer = InGameTimer.getInstance();
        
        AdvancementNode advancement = this.tree.get(entry.getKey());
        AdvancementProgress advancementProgress = entry.getValue();
        assert advancement != null;
        advancementProgress.update(advancement.advancement().requirements());

        if (advancementProgress.isDone() && timer.getStatus() != TimerStatus.NONE) {
            // Events system
            GameInstance.getInstance().callEvents("advancement", factory -> advancement.holder().id().getPath().equalsIgnoreCase(factory.getDataValue("advancement")));

            // For Timelines
            if (Objects.equals(advancement.holder().id().getPath(), "story/follow_ender_eye")) {
                timer.tryInsertNewTimeline("enter_stronghold");
            } else if (Objects.equals(advancement.holder().id().getPath(), "nether/find_bastion")) {
                timer.tryInsertNewTimeline("enter_bastion");
            } else if (Objects.equals(advancement.holder().id().getPath(), "nether/find_fortress")) {
                timer.tryInsertNewTimeline("enter_fortress");
            }

            timer.tryInsertNewAdvancement(advancement.holder().id().toString(), null, advancement.advancement().display().isPresent());
            if (timer.isCoop() && advancement.advancement().display().isPresent()) {
                TimerPacketUtils.sendClient2ServerPacket(minecraft, new TimerAchieveAdvancementPacket(advancement.holder()));
            }

            // Custom Json category
            if (timer.getCategory().getConditionJson() != null) {
                for (CategoryCondition.Condition<?> condition : timer.getCustomCondition().map(CategoryCondition::getConditionList).orElse(Lists.newArrayList())) {
                    if (condition instanceof AdvancementCategoryCondition) {
                        timer.updateCondition((AdvancementCategoryCondition) condition, advancement.holder());
                    }
                }
                timer.checkConditions();
            }

            //How Did We Get Here
            if (timer.getCategory() == RunCategories.HOW_DID_WE_GET_HERE && Objects.equals(advancement.holder().id().toString(), Identifier.parse("nether/all_effects").toString())) {
                InGameTimer.complete();
            }

            //Hero of Village
            if (timer.getCategory() == RunCategories.HERO_OF_VILLAGE && Objects.equals(advancement.holder().id().toString(), Identifier.parse("adventure/hero_of_the_village").toString())) {
                InGameTimer.complete();
            }

            //Arbalistic
            if (timer.getCategory() == RunCategories.ARBALISTIC && Objects.equals(advancement.holder().id().toString(), Identifier.parse("adventure/arbalistic").toString())) {
                InGameTimer.complete();
            }

            //Cover Me In Debris
            if (timer.getCategory() == RunCategories.COVER_ME_IN_DEBRIS && Objects.equals(advancement.holder().id().toString(), Identifier.parse("nether/netherite_armor").toString())) {
                InGameTimer.complete();
            }
        }
        return entry;
    }

    @Inject(at = @At("RETURN"), method = "update")
    public void onComplete(ClientboundUpdateAdvancementsPacket packet, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        int maxCount = timer.getMoreData(7441) == 0 ? 80 : timer.getMoreData(7441);

        // All Advancements
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.ALL_ADVANCEMENTS) {
            if (this.getCompleteAdvancementsCount() >= maxCount) InGameTimer.complete();
        }

        // Half%
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.HALF) {
            if (this.getCompleteAdvancementsCount() >= Mth.ceil(maxCount / 2.0f)) InGameTimer.complete();
        }

        // PogLoot Quater
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.POGLOOT_QUATER) {
            if (this.getCompleteAdvancementsCount() >= Mth.ceil(maxCount / 4.0f)) InGameTimer.complete();
        }
    }

    @Unique
    private int getCompleteAdvancementsCount() {
        Set<String> completedAdvancements = Sets.newHashSet();
        for (Map.Entry<String, TimerAdvancementTracker.AdvancementTrack> track : InGameTimer.getInstance().getAdvancementsTracker().getAdvancements().entrySet()) {
            if (track.getValue().isAdvancement() && track.getValue().isComplete()) completedAdvancements.add(track.getKey());
        }
        for (AdvancementNode advancement : this.getTree().nodes()) {
            if (this.progress.containsKey(advancement.advancement()) && advancement.advancement().display().isPresent()) {
                AdvancementProgress advancementProgress = this.progress.get(advancement.advancement());

                advancementProgress.update(advancement.advancement().requirements());
                String advancementID = advancement.holder().id().toString();
                if (advancementProgress.isDone() && completedAdvancements.contains(advancementID)) {
                    completedAdvancements.add(advancementID);
                    InGameTimer.getInstance().tryInsertNewAdvancement(advancementID, null, true);
                }
            }
        }
        return completedAdvancements.size();
    }
}
