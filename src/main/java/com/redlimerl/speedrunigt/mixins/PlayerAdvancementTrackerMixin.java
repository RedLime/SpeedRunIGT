package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.*;
import com.redlimerl.speedrunigt.timer.category.condition.AdvancementCategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.LinkedHashMap;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {

    @Shadow private ServerPlayerEntity owner;

    @ModifyArgs(method = "endTrackingCompleted", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/Criterion$ConditionsContainer;<init>(Lnet/minecraft/advancement/criterion/CriterionConditions;Lnet/minecraft/advancement/Advancement;Ljava/lang/String;)V"))
    private void getCriteria(Args args) {
        Advancement advancement = args.get(1);
        String criteriaKey = args.get(2);

        LinkedHashMap<String, TimerAdvancementTracker.AdvancementTrack> advancements = InGameTimer.getInstance().getAdvancementsTracker().getAdvancements();
        if (advancements.containsKey(advancement.getId().toString())) {
            TimerAdvancementTracker.AdvancementTrack track = advancements.get(advancement.getId().toString());
            if (track.isComplete() || track.isCompletedCriteria(criteriaKey)) return;
        }

        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) {
            InGameTimer.getInstance().tryInsertNewAdvancement(advancement.getId().toString(), criteriaKey, advancement.getDisplay() != null, false);
        }

        InGameTimer timer = InGameTimer.getInstance();
        // Custom Json category
        if (timer.getCategory().getConditionJson() != null) {
            for (CategoryCondition.Condition<?> condition : timer.getCustomCondition().getConditionList()) {
                if (condition instanceof AdvancementCategoryCondition) {
                    if (timer.updateCondition((AdvancementCategoryCondition) condition, advancement) && timer.isCoop())
                        TimerPacketHandler.serverSend(owner.server.getPlayerManager().getPlayerList(), InGameTimer.getInstance(), InGameTimer.getCompletedInstance());
                }
            }
            timer.checkConditions();
        }
    }

    private boolean prevDone = false;
    @Redirect(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementProgress;isDone()Z", ordinal = 1))
    public boolean onGrant(AdvancementProgress instance) {
        prevDone = instance.isDone();
        if (InGameTimerUtils.IS_SHARING_ADVANCEMENT) return false;
        return prevDone;
    }

    @Inject(method = "grantCriterion", at = @At("TAIL"))
    public void onGrant(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (!InGameTimerUtils.IS_SHARING_ADVANCEMENT && prevDone && advancement.getDisplay() != null && InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getStatus() != TimerStatus.NONE) {
            InGameTimerUtils.IS_SHARING_ADVANCEMENT = true;
            for (ServerPlayerEntity serverPlayerEntity : owner.server.getPlayerManager().getPlayerList()) {
                if (this.owner == serverPlayerEntity) continue;
                serverPlayerEntity.getAdvancementTracker().grantCriterion(advancement, criterionName);
            }
            InGameTimerUtils.IS_SHARING_ADVANCEMENT = false;
        }
        prevDone = false;
    }
}
