package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.StatCategoryCondition;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ServerStatHandler.class)
public abstract class ServerStatHandlerMixin extends StatHandler {

    @Shadow
    private static <T> Identifier getStatId(Stat<T> stat) {
        return null;
    }

    private int updateTick = 0;

    @Inject(method = "setStat", at = @At("TAIL"))
    public void onUpdate(PlayerEntity player, Stat<?> stat, int value, CallbackInfo ci) {
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
            InGameTimerUtils.updateStatsJson(timer);
            this.updateTick = 0;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions"})
    private JsonObject getStatJson() {
        HashMap<StatType, JsonObject> map = Maps.newHashMap();
        for (Object2IntMap.Entry entry : this.statMap.object2IntEntrySet()) {
            Stat stat = (Stat)entry.getKey();
            map.computeIfAbsent(stat.getType(), statType -> new JsonObject()).addProperty(getStatId(stat).toString(), entry.getIntValue());
        }
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry entry : map.entrySet()) {
            jsonObject.add(Registry.STAT_TYPE.getId((StatType<?>)entry.getKey()).toString(), (JsonElement)entry.getValue());
        }
        return jsonObject;
    }
}
