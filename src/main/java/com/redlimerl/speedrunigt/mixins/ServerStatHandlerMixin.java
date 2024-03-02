package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.StatCategoryCondition;
import net.minecraft.class_0_1685;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerStatHandler.class)
public abstract class ServerStatHandlerMixin extends StatHandler {

    @Shadow @Final private static Logger LOGGER;

    private int updateTick = 0;

    @Inject(method = "setStat", at = @At("TAIL"))
    public void onUpdate(PlayerEntity playerEntity, Stat stat, int i, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        // Custom Json category
        if (timer.getCategory().getConditionJson() != null) {
            JsonObject jsonObject = getStatJson();
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

    private JsonObject getStatJson() {
        JsonObject jsonObject = new JsonObject();

        for (Map.Entry<Stat, class_0_1685> entry : this.field_15431.entrySet()) {
            if (entry.getValue().method_0_6345() != null) {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("value", entry.getValue().method_0_6342());

                try {
                    jsonObject2.add("progress", entry.getValue().method_0_6345().method_0_6355());
                } catch (Throwable var6) {
                    LOGGER.warn("Couldn't save statistic {}: error serializing progress", entry.getKey().method_0_6336(), var6);
                }

                jsonObject.add(entry.getKey().field_0_6932, jsonObject2);
            } else {
                jsonObject.addProperty(entry.getKey().field_0_6932, entry.getValue().method_0_6342());
            }
        }
        return jsonObject;
    }
}
