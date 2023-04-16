package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.StatCategoryCondition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.JsonIntSerializable;
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

    @Inject(method = "method_8300", at = @At("TAIL"))
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

        for (Map.Entry<Stat, JsonIntSerializable> entry : this.field_9047.entrySet()) {
            if (entry.getValue().getJsonElementProvider() != null) {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("value", entry.getValue().getValue());

                try {
                    jsonObject2.add("progress", entry.getValue().getJsonElementProvider().write());
                } catch (Throwable var6) {
                    LOGGER.warn("Couldn't save statistic {}: error serializing progress", entry.getKey().getText(), var6);
                }

                jsonObject.add(entry.getKey().name, jsonObject2);
            } else {
                jsonObject.addProperty(entry.getKey().name, entry.getValue().getValue());
            }
        }
        return jsonObject;
    }
}
