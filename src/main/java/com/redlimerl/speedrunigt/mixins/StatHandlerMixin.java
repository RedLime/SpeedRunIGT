package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerClientUtils;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.StatCategoryCondition;
import net.minecraft.advancement.AchievementsAndCriterions;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(StatHandler.class)
public abstract class StatHandlerMixin {
    @Shadow public abstract Map<Stat, Integer> method_1734();

    private int updateTick = 0;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        SpeedRunIGT.debug("Detected Achievements: "+ AchievementsAndCriterions.ACHIEVEMENTS.size());
        InGameTimer.getInstance().updateMoreData(7441, AchievementsAndCriterions.ACHIEVEMENTS.size());
    }

    @Inject(method = "method_1733", at = @At("TAIL"))
    public void onUpdate(Map<?, ?> stat, Stat i, int par3, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        // Custom Json category
        if (timer.getCategory().getConditionJson() != null) {
            JsonObject jsonObject = this.getStatJson();
            for (CategoryCondition.Condition<?> condition : timer.getCustomCondition().map(CategoryCondition::getConditionList).orElse(Lists.newArrayList())) {
                if (condition instanceof StatCategoryCondition) {
                    timer.updateCondition((StatCategoryCondition) condition, jsonObject);
                }
            }
            timer.checkConditions();
        }

        if (this.updateTick++ > 20) {
            InGameTimerClientUtils.STATS_UPDATE = null;
            this.updateTick = 0;
        }
    }

    @Unique
    private JsonObject getStatJson() {
        Map<Stat, Integer> map = this.method_1734();
        JsonObject jsonObject = new JsonObject();

        for (Map.Entry<Stat, Integer> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.add(Integer.toString(entry.getKey().id), new JsonPrimitive(map.get(entry.getKey())));
            }
        }
        return jsonObject;
    }
}
