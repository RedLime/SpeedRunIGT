package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunSplitTypes;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
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

@Mixin(ClientAdvancementManager.class)
public abstract class ClientAdvancementManagerMixin {

    @Shadow @Final private AdvancementManager manager;

    @Shadow public abstract AdvancementManager getManager();

    @Shadow @Final private Map<Advancement, AdvancementProgress> advancementProgresses;

    @Redirect(method = "onAdvancements", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;"))
    public Object advancement(Map.Entry<Identifier, AdvancementProgress> entry) {
        InGameTimer timer = InGameTimer.getInstance();
        
        Advancement advancement = this.manager.get(entry.getKey());
        AdvancementProgress advancementProgress = entry.getValue();
        assert advancement != null;
        advancementProgress.init(advancement.getCriteria(), advancement.getRequirements());

        if (advancementProgress.isDone() && timer.getStatus() != TimerStatus.NONE) {

            //How Did We Get Here
            if (timer.getCategory() == RunCategories.HOW_DID_WE_GET_HERE && Objects.equals(advancement.getId().toString(), new Identifier("nether/all_effects").toString())) {
                InGameTimer.complete();
            }

            //Hero of Village
            if (timer.getCategory() == RunCategories.HERO_OF_VILLAGE && Objects.equals(advancement.getId().toString(), new Identifier("adventure/hero_of_the_village").toString())) {
                InGameTimer.complete();
            }

            //Arbalistic
            if (timer.getCategory() == RunCategories.ARBALISTIC && Objects.equals(advancement.getId().toString(), new Identifier("adventure/arbalistic").toString())) {
                InGameTimer.complete();
            }


            //Timer Split
            if (timer.getCategory() == RunCategories.ANY) {
                if (Objects.equals(advancement.getId().toString(), new Identifier("story/enter_the_nether").toString())) {
                    timer.getTimerSplit().tryUpdateSplit(RunSplitTypes.ENTER_NETHER, timer.getInGameTime());
                }
                else if (Objects.equals(advancement.getId().toString(), new Identifier("story/enter_the_end").toString())) {
                    timer.getTimerSplit().tryUpdateSplit(RunSplitTypes.ENTER_END, timer.getInGameTime());
                }
                else if (Objects.equals(advancement.getId().toString(), new Identifier("story/follow_ender_eye").toString())) {
                    timer.getTimerSplit().tryUpdateSplit(RunSplitTypes.ENTER_STRONG_HOLD, timer.getInGameTime());
                }
                else if (Objects.equals(advancement.getId().toString(), new Identifier("nether/find_fortress").toString())) {
                    timer.getTimerSplit().tryUpdateSplit(RunSplitTypes.ENTER_FORTRESS, timer.getInGameTime());
                }
                else if (Objects.equals(advancement.getId().toString(), new Identifier("nether/find_bastion").toString())) {
                    timer.getTimerSplit().tryUpdateSplit(RunSplitTypes.ENTER_BASTION, timer.getInGameTime());
                }
            }
        }
        return entry.getValue();
    }

    @Inject(at = @At("RETURN"), method = "onAdvancements")
    public void onComplete(AdvancementUpdateS2CPacket packet, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        //All Advancements
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.ALL_ADVANCEMENTS) {
            if (getCompleteAdvancementsCount() >= 91) InGameTimer.complete();
        }

        //Half%
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.HALF) {
            if (getCompleteAdvancementsCount() >= 46) InGameTimer.complete();
        }

        //(PogLoot) Quater
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.POGLOOT_QUATER) {
            if (getCompleteAdvancementsCount() >= 23) InGameTimer.complete();
        }
    }

    private int getCompleteAdvancementsCount() {
        int count = 0;
        for (Advancement advancement : this.getManager().getAdvancements()) {
            if (this.advancementProgresses.containsKey(advancement) && advancement.getDisplay() != null && !advancement.getId().getNamespace().startsWith("recipes")) {
                AdvancementProgress advancementProgress = this.advancementProgresses.get(advancement);

                advancementProgress.init(advancement.getCriteria(), advancement.getRequirements());
                if (advancementProgress.isDone()) count++;
            }
        }
        return count;
    }
}
