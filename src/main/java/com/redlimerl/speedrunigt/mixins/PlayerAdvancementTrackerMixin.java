package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerAchieveCriteriaPacket;
import net.minecraft.advancement.AdvancementFile;
import net.minecraft.advancement.SimpleAdvancement;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Map;

@Mixin(AdvancementFile.class)
public class PlayerAdvancementTrackerMixin {

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "method_14928", at = @At("RETURN"))
    private void onBegin(CallbackInfo ci) {
        int count = 0;
        for (SimpleAdvancement advancement : this.server.method_14910().method_14940()) {
            if (advancement.getDisplay() != null) count++;
        }
        InGameTimer.getInstance().updateMoreData(7441, count);
    }

    @ModifyArgs(method = "method_14929", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/Criterion$class_3353;<init>(Lnet/minecraft/advancement/criterion/CriterionInstance;Lnet/minecraft/advancement/SimpleAdvancement;Ljava/lang/String;)V"))
    private void getCriteria(Args args) {
        SimpleAdvancement advancement = args.get(1);
        String criteriaKey = args.get(2);

        Map<String, TimerAdvancementTracker.AdvancementTrack> advancements = InGameTimer.getInstance().getAdvancementsTracker().getAdvancements();
        if (advancements.containsKey(advancement.getIdentifier().toString())) {
            TimerAdvancementTracker.AdvancementTrack track = advancements.get(advancement.getIdentifier().toString());
            if (track.isComplete() || track.isCompletedCriteria(criteriaKey)) return;
        }

        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.tryInsertNewAdvancement(advancement.getIdentifier().toString(), criteriaKey, advancement.getDisplay() != null);
            if (timer.isCoop()) TimerPacketUtils.sendServer2ClientPacket(this.server, new TimerAchieveCriteriaPacket(advancement.getIdentifier().toString(), criteriaKey, advancement.getDisplay() != null));
        }
    }
}
