package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunSplitTypes;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.SimpleAdvancement;
import net.minecraft.class_3295;
import net.minecraft.class_3328;
import net.minecraft.network.packet.c2s.play.AdvancementUpdatePacket;
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

    @Shadow @Final private class_3328 field_16128;

    @Shadow @Final private Map<SimpleAdvancement, AdvancementProgress> field_16129;

    @Redirect(method = "onProgressUpdate", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;"))
    public Object advancement(Map.Entry<Identifier, AdvancementProgress> entry) {
        InGameTimer timer = InGameTimer.getInstance();

        SimpleAdvancement advancement = this.field_16128.method_14814(entry.getKey());
        AdvancementProgress advancementProgress = entry.getValue();
        assert advancement != null;
        advancementProgress.method_14836(advancement.getCriteria(), advancement.getRequirements());

        if (advancementProgress.method_14833() && timer.getStatus() != TimerStatus.NONE) {

            //How Did We Get Here
            if (timer.getCategory() == RunCategories.HOW_DID_WE_GET_HERE && Objects.equals(advancement.getIdentifier().toString(), new Identifier("nether/all_effects").toString())) {
                InGameTimer.complete();
            }

            //Timer Split
            if (timer.getCategory() == RunCategories.ANY) {
                if (Objects.equals(advancement.getIdentifier().toString(), new Identifier("story/follow_ender_eye").toString())) {
                    timer.getTimerSplit().tryUpdateSplit(RunSplitTypes.ENTER_STRONG_HOLD, timer.getInGameTime());
                }
                else if (Objects.equals(advancement.getIdentifier().toString(), new Identifier("nether/find_fortress").toString())) {
                    timer.getTimerSplit().tryUpdateSplit(RunSplitTypes.ENTER_FORTRESS, timer.getInGameTime());
                }
                else if (Objects.equals(advancement.getIdentifier().toString(), new Identifier("nether/find_bastion").toString())) {
                    timer.getTimerSplit().tryUpdateSplit(RunSplitTypes.ENTER_BASTION, timer.getInGameTime());
                }
            }
        }
        return entry.getValue();
    }

    @Inject(at = @At("RETURN"), method = "onProgressUpdate")
    public void onComplete(AdvancementUpdatePacket advancementUpdatePacket, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        //All Advancements
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.ALL_ADVANCEMENTS) {
            if (getCompleteAdvancementsCount() >= 54) InGameTimer.complete();
        }

        //Half%
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.HALF) {
            if (getCompleteAdvancementsCount() >= 27) InGameTimer.complete();
        }

        //(PogLoot) Quater
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.POGLOOT_QUATER) {
            if (getCompleteAdvancementsCount() >= 14) InGameTimer.complete();
        }
    }

    private int getCompleteAdvancementsCount() {
        int count = 0;
        for (SimpleAdvancement advancement : this.field_16128.method_20270()) {
            if (this.field_16129.containsKey(advancement) && advancement.getParent() != null && !advancement.getIdentifier().getPath().startsWith("recipes")) {
                AdvancementProgress advancementProgress = this.field_16129.get(advancement);

                advancementProgress.method_14836(advancement.getCriteria(), advancement.getRequirements());
                if (advancementProgress.method_14833()) count++;
            }
        }
        return count;
    }
}