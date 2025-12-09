package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.StatCategoryCondition;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Objects;

@Mixin(ServerStatHandler.class)
public abstract class ServerStatHandlerMixin extends StatHandler {

    @Unique
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

        if (timer.getStatus() == TimerStatus.NONE || timer.getStatus() == TimerStatus.COMPLETED_LEGACY) return;

        // All Blocks
        if (timer.getCategory() == RunCategories.ALL_BLOCKS) {
            if (player instanceof ServerPlayerEntity) {
                if (RunCategories.ALL_BLOCKS.isCompleted(player.getEntityWorld().getServer()))
                    InGameTimer.complete();
            }
        }

        if (this.updateTick++ > 20) {
            InGameTimerUtils.updateStatsJson(timer);
            this.updateTick = 0;
        }
    }

    @Unique
    private JsonObject getStatJson() {
        HashMap<StatType<?>, JsonObject> map = Maps.newHashMap();
        for (Object2IntMap.Entry<?> entry : this.statMap.object2IntEntrySet()) {
            Stat<?> stat = (Stat<?>)entry.getKey();
            map.computeIfAbsent(stat.getType(), statType -> new JsonObject()).addProperty(stat.getValue().toString(), entry.getIntValue());
        }
        JsonObject jsonObject = new JsonObject();
        map.forEach((key, value) -> jsonObject.add(Objects.requireNonNull(Registries.STAT_TYPE.getId(key)).toString(), value));
        return jsonObject;
    }
}
