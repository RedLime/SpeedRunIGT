package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import net.minecraft.achievement.Achievement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatHandler.class)
public abstract class StatHandlerMixin {
    @Shadow
    public abstract boolean method_8298(Achievement achievement);
    /**
     * @author Void_X_Walker
     * @reason Achievements
     */
    @Inject(method = "method_8302", at = @At("HEAD"))
    public void onComplete(PlayerEntity playerEntity, Stat stat, int i, CallbackInfo ci){
        if(stat.isAchievement()){
            InGameTimer timer = InGameTimer.getInstance();

            if (timer.getStatus() == TimerStatus.NONE) return;

            // For Timeline
            timer.tryInsertNewAdvancement(stat.name, null);

            //All Advancements
            if (timer.getCategory() == RunCategories.ALL_ACHIEVEMENTS) {
                if (getCompleteAdvancementsCount()+1 >= 34) InGameTimer.complete();
            }

            //Half%
            if (timer.getCategory() == RunCategories.HALF) {
                if (getCompleteAdvancementsCount()+1 >= 17) InGameTimer.complete();
            }
        }

    }
    private int getCompleteAdvancementsCount() {
        int count = 0;
        for (Object obj : Stats.ALL) {
            Stat stat = (Stat) obj;
            if(stat.isAchievement()&&this.method_8298((Achievement) stat)){
                count++;
            }
        }
        return count;
    }
}
