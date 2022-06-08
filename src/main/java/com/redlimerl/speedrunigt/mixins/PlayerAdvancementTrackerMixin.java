package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerAchieveCriteriaPacket;
import net.minecraft.class_3326;
import net.minecraft.class_3347;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.LinkedHashMap;

@Mixin(class_3347.class)
public class PlayerAdvancementTrackerMixin {

    @Shadow private ServerPlayerEntity field_16375;

    @Shadow @Final private MinecraftServer field_16369;

    @Inject(method = "method_14928", at = @At("RETURN"))
    private void onBegin(CallbackInfo ci) {
        int count = 0;
        for (class_3326 advancement : this.field_16369.method_14910().method_14940()) {
            if (advancement.method_14796() != null) count++;
        }
        InGameTimer.getInstance().updateMoreData(7441, count);
    }

    @ModifyArgs(method = "method_14929", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3352$class_3353;<init>(Lnet/minecraft/class_3354;Lnet/minecraft/class_3326;Ljava/lang/String;)V"))
    private void getCriteria(Args args) {
        class_3326 advancement = args.get(1);
        String criteriaKey = args.get(2);

        LinkedHashMap<String, TimerAdvancementTracker.AdvancementTrack> advancements = InGameTimer.getInstance().getAdvancementsTracker().getAdvancements();
        if (advancements.containsKey(advancement.method_14801().toString())) {
            TimerAdvancementTracker.AdvancementTrack track = advancements.get(advancement.method_14801().toString());
            if (track.isComplete() || track.isCompletedCriteria(criteriaKey)) return;
        }

        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.NONE) {
            timer.tryInsertNewAdvancement(advancement.method_14801().toString(), criteriaKey, advancement.method_14796() != null);
            if (timer.isCoop()) TimerPacketUtils.sendServer2ClientPacket(field_16375.server, new TimerAchieveCriteriaPacket(advancement.method_14801().toString(), criteriaKey, advancement.method_14796() != null));
        }
    }
}
