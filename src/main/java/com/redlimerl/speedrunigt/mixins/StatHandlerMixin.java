package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.achievement.Achievement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.JsonIntSerializable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(StatHandler.class)
public abstract class StatHandlerMixin {
    @Shadow @Final protected Map<Stat, JsonIntSerializable> field_7025;


    @Shadow public abstract boolean method_6396(Achievement achievement);
    /**
     * @author Void_X_Walker
     * @reason Achievements
     */
    @Inject(method = "method_6399", at = @At("HEAD"))
    public void onComplete(PlayerEntity playerEntity, Stat stat, int i, CallbackInfo ci){
        if(stat.isAchievement()){
            InGameTimer timer = InGameTimer.getInstance();

            //All Advancements
            if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategory.ALL_ACHIEVEMENTS) {
                if (getCompleteAdvancementsCount()+1>= 34) InGameTimer.complete();
            }

            //Half%
            if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategory.HALF) {
                if (getCompleteAdvancementsCount()+1 >= 17) InGameTimer.complete();
            }
        }

    }
    private int getCompleteAdvancementsCount() {
        int count = 0;
        for (Stat stat : Stats.ALL) {

            if(stat.isAchievement()&&this.method_6396((Achievement) stat)){
                count++;
            }
        }
        System.out.println(count);
        return count;
    }
}
