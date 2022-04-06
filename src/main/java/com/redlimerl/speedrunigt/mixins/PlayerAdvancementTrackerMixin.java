package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerAchieveCriteriaPacket;
import net.minecraft.advancement.AdvancementFile;
import net.minecraft.advancement.SimpleAdvancement;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.LinkedHashMap;

@Mixin(AdvancementFile.class)
public class PlayerAdvancementTrackerMixin {

    @Shadow private ServerPlayerEntity player;

    @ModifyArgs(method = "method_14929", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/Criterion$class_3353;<init>(Lnet/minecraft/advancement/criterion/CriterionInstance;Lnet/minecraft/advancement/SimpleAdvancement;Ljava/lang/String;)V"))
    private void getCriteria(Args args) {
        SimpleAdvancement advancement = args.get(1);
        String criteriaKey = args.get(2);

        LinkedHashMap<String, TimerAdvancementTracker.AdvancementTrack> advancements = InGameTimer.getInstance().getAdvancementsTracker().getAdvancements();
        if (advancements.containsKey(advancement.getIdentifier().toString())) {
            TimerAdvancementTracker.AdvancementTrack track = advancements.get(advancement.getIdentifier().toString());
            if (track.isComplete() || track.isCompletedCriteria(criteriaKey)) return;
        }

        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.tryInsertNewAdvancement(advancement.getIdentifier().toString(), criteriaKey, advancement.getDisplay() != null);
            if (timer.isCoop()) TimerPacketUtils.sendServer2ClientPacket(player.server, new TimerAchieveCriteriaPacket(advancement.getIdentifier().toString(), criteriaKey, advancement.getDisplay() != null));
        }
    }
}
