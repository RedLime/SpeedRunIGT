package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerAchieveCriteriaPacket;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
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

        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.tryInsertNewAdvancement(advancement.getId().toString(), criteriaKey, advancement.getDisplay() != null);
            if (timer.isCoop()) TimerPacketUtils.sendServer2ClientPacket(owner.server, new TimerAchieveCriteriaPacket(advancement.getId().toString(), criteriaKey, advancement.getDisplay() != null));
        }
    }
}
