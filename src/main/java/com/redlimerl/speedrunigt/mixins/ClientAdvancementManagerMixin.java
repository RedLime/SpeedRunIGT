package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.condition.AdvancementCategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketUtils;
import com.redlimerl.speedrunigt.timer.packet.packets.TimerAchieveAdvancementPacket;
import net.minecraft.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;

@Mixin(class_3295.class)
public abstract class ClientAdvancementManagerMixin {

    @Shadow @Final private MinecraftClient field_16127;

    @Shadow @Final private class_3328 field_16128;

    @Redirect(method = "method_14667", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;"))
    public Object advancement(Map.Entry<Identifier, class_3334> entry) {
        InGameTimer timer = InGameTimer.getInstance();

        class_3326 advancement = this.field_16128.method_14814(entry.getKey());
        class_3334 advancementProgress = entry.getValue();
        assert advancement != null;
        advancementProgress.method_14836(advancement.method_14799(), advancement.method_14802());

        if (advancementProgress.method_14833() && timer.getStatus() != TimerStatus.NONE) {

            // For Timelines
            if (timer.getCategory() == RunCategories.ANY) {
                if (Objects.equals(advancement.method_14801().getPath(), "story/follow_ender_eye")) {
                    timer.tryInsertNewTimeline("enter_stronghold");
                } else if (Objects.equals(advancement.method_14801().getPath(), "nether/find_fortress")) {
                    timer.tryInsertNewTimeline("enter_fortress");
                }
            }
            timer.tryInsertNewAdvancement(advancement.method_14801().toString(), null, advancement.method_14796() != null);
            InGameTimerUtils.COMPLETED_ADVANCEMENTS.add(advancement.method_14801().toString());
            if (timer.isCoop() && advancement.method_14796() != null
                    && (timer.getCategory() == RunCategories.ALL_ADVANCEMENTS || timer.getCategory() == RunCategories.HALF || timer.getCategory() == RunCategories.POGLOOT_QUATER)) {
                TimerPacketUtils.sendClient2ServerPacket(field_16127, new TimerAchieveAdvancementPacket(advancement));
            }

            // Custom Json category
            if (timer.getCategory().getConditionJson() != null) {
                for (CategoryCondition.Condition<?> condition : timer.getCustomCondition().getConditionList()) {
                    if (condition instanceof AdvancementCategoryCondition) {
                        timer.updateCondition((AdvancementCategoryCondition) condition, advancement);
                    }
                }
                timer.checkConditions();
            }

            //How Did We Get Here
            if (timer.getCategory() == RunCategories.HOW_DID_WE_GET_HERE && Objects.equals(advancement.method_14801().toString(), new Identifier("nether/all_effects").toString())) {
                InGameTimer.complete();
            }
        }
        return entry.getValue();
    }

    @Inject(at = @At("RETURN"), method = "method_14667")
    public void onComplete(class_3336 arg, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        //All Advancements
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.ALL_ADVANCEMENTS) {
            if (getCompleteAdvancementsCount() >= 55) InGameTimer.complete();
        }

        //Half%
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.HALF) {
            if (getCompleteAdvancementsCount() >= 28) InGameTimer.complete();
        }

        //(PogLoot) Quater
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.POGLOOT_QUATER) {
            if (getCompleteAdvancementsCount() >= 14) InGameTimer.complete();
        }
    }

    private int getCompleteAdvancementsCount() {
        int count = 0;
        for (Map.Entry<String, TimerAdvancementTracker.AdvancementTrack> track : InGameTimer.getInstance().getAdvancementsTracker().getAdvancements().entrySet()) {
            if (track.getValue().isAdvancement() && track.getValue().isComplete()) count++;
        }
        return count;
    }
}
