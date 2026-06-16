package com.redlimerl.speedrunigt.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerAchieveCriteriaPacket;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {

    @Shadow private ServerPlayer player;

    @Inject(method = "registerListeners(Lnet/minecraft/server/ServerAdvancementManager;)V", at = @At("RETURN"))
    private void onBegin(ServerAdvancementManager manager, CallbackInfo ci) {
        int count = 0;
        for (AdvancementHolder advancement : manager.getAllAdvancements()) {
            if (advancement.value().display().isPresent()) count++;
        }
        SpeedRunIGT.debug("Detected Advancements: " + count);
        InGameTimer.getInstance().updateMoreData(7441, count);
    }

    @Inject(method = "unregisterListeners", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;removeListener(Lnet/minecraft/advancements/triggers/CriterionTrigger;Lnet/minecraft/server/PlayerAdvancements$TriggerInstanceKey;)V"))
    private void getCriteria(AdvancementHolder holder, CallbackInfo ci, @Local(name = "entry") Map.Entry<String, Criterion<?>> entry) {
        String criteriaKey = entry.getKey();

        Map<String, TimerAdvancementTracker.AdvancementTrack> advancements = InGameTimer.getInstance().getAdvancementsTracker().getAdvancements();
        if (advancements.containsKey(holder.id().toString())) {
            TimerAdvancementTracker.AdvancementTrack track = advancements.get(holder.id().toString());
            if (track.isComplete() || track.isCompletedCriteria(criteriaKey)) return;
        }

        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.tryInsertNewAdvancement(holder.id().toString(), criteriaKey, holder.value().display().isPresent());
            if (timer.isCoop()) TimerPacketUtils.sendServer2ClientPacket(Objects.requireNonNull(player.level().getServer()), new TimerAchieveCriteriaPacket(holder.id().toString(), criteriaKey, holder.value().display().isPresent()));
        }
    }
}
