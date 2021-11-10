package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.timer.TimerStatus;
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

    @Redirect(method = "onAdvancements", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;"))
    public Object advancement(Map.Entry<Identifier, AdvancementProgress> entry) {
        Advancement advancement = this.manager.get(entry.getKey());
        AdvancementProgress advancementProgress = entry.getValue();
        assert advancement != null;
        advancementProgress.init(advancement.getCriteria(), advancement.getRequirements());

        if (advancementProgress.isDone() && InGameTimer.INSTANCE.getStatus() != TimerStatus.NONE) {

            //How Did We Get Here
            if (InGameTimer.INSTANCE.getCategory() == RunCategory.HOW_DID_WE_GET_HERE && Objects.equals(advancement.getId().toString(), new Identifier("nether/all_effects").toString())) {
                InGameTimer.INSTANCE.complete();
            }

            //Hero of Village
            if (InGameTimer.INSTANCE.getCategory() == RunCategory.HERO_OF_VILLAGE && Objects.equals(advancement.getId().toString(), new Identifier("adventure/hero_of_the_village").toString())) {
                InGameTimer.INSTANCE.complete();
            }

            //Arbalistic
            if (InGameTimer.INSTANCE.getCategory() == RunCategory.ARBALISTIC && Objects.equals(advancement.getId().toString(), new Identifier("adventure/arbalistic").toString())) {
                InGameTimer.INSTANCE.complete();
            }
        }
        return entry.getValue();
    }

    @Inject(at = @At("RETURN"), method = "onAdvancements")
    public void onComplete(AdvancementUpdateS2CPacket packet, CallbackInfo ci) {
        //All Advancements
        if (InGameTimer.INSTANCE.getStatus() != TimerStatus.NONE && InGameTimer.INSTANCE.getCategory() == RunCategory.ALL_ADVANCEMENTS) {
            int completes = this.getManager().getAdvancements().stream()
                    .filter(advancement -> advancement.getDisplay() != null).toArray().length;
            if (completes == 80) InGameTimer.INSTANCE.complete();
        }

        //Half%
        if (InGameTimer.INSTANCE.getStatus() != TimerStatus.NONE && InGameTimer.INSTANCE.getCategory() == RunCategory.HALF) {
            int completes = this.getManager().getAdvancements().stream()
                    .filter(advancement -> advancement.getDisplay() != null).toArray().length;
            if (completes == 40) InGameTimer.INSTANCE.complete();
        }
    }

}
