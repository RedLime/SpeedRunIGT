package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerAchieveCriteriaPacket;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Map;
import java.util.Objects;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {

    @Shadow private ServerPlayerEntity owner;

    @Inject(method = "beginTrackingAllAdvancements", at = @At("RETURN"))
    private void onBegin(ServerAdvancementLoader advancementLoader, CallbackInfo ci) {
        int count = 0;
        for (AdvancementEntry advancement : advancementLoader.getAdvancements()) {
            if (advancement.value().display().isPresent()) count++;
        }
        SpeedRunIGT.debug("Detected Advancements: " + count);
        InGameTimer.getInstance().updateMoreData(7441, count);
    }

    @ModifyArgs(method = "endTrackingCompleted(Lnet/minecraft/advancement/AdvancementEntry;Ljava/lang/String;Lnet/minecraft/advancement/AdvancementCriterion;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/Criterion$ConditionsContainer;<init>(Lnet/minecraft/advancement/criterion/CriterionConditions;Lnet/minecraft/advancement/AdvancementEntry;Ljava/lang/String;)V"))
    private void getCriteria(Args args) {
        AdvancementEntry advancement = args.get(1);
        String criteriaKey = args.get(2);

        Map<String, TimerAdvancementTracker.AdvancementTrack> advancements = InGameTimer.getInstance().getAdvancementsTracker().getAdvancements();
        if (advancements.containsKey(advancement.id().toString())) {
            TimerAdvancementTracker.AdvancementTrack track = advancements.get(advancement.id().toString());
            if (track.isComplete() || track.isCompletedCriteria(criteriaKey)) return;
        }

        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.tryInsertNewAdvancement(advancement.id().toString(), criteriaKey, advancement.value().display().isPresent());
            if (timer.isCoop()) TimerPacketUtils.sendServer2ClientPacket(Objects.requireNonNull(owner.getEntityWorld().getServer()), new TimerAchieveCriteriaPacket(advancement.id().toString(), criteriaKey, advancement.value().display().isPresent()));
        }
    }
}
