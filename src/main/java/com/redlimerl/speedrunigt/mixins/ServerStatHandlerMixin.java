package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.StatCategoryCondition;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.class_4472;
import net.minecraft.class_4474;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.StatHandler;
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
    private static <T> Identifier method_21418(class_4472<T> arg) {
        return null;
    }

    @Inject(method = "method_8300", at = @At("TAIL"))
    public void onUpdate(PlayerEntity playerEntity, class_4472<?> arg, int i, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        // Custom Json category
        if (timer.getCategory().getConditionJson() != null) {
            JsonObject jsonObject = getStatJson();
            for (CategoryCondition.Condition<?> condition : timer.getCustomCondition().getConditionList()) {
                if (condition instanceof StatCategoryCondition) {
                    timer.updateCondition((StatCategoryCondition) condition, jsonObject);
                }
            }
            timer.checkConditions();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions"})
    private JsonObject getStatJson() {
        HashMap<class_4474, JsonObject> map = Maps.newHashMap();
        for (Object2IntMap.Entry entry : this.field_22141.object2IntEntrySet()) {
            class_4472 class_44722 = (class_4472)entry.getKey();
            map.computeIfAbsent(class_44722.method_21419(), arg -> new JsonObject()).addProperty(method_21418(class_44722).toString(), entry.getIntValue());
        }
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry entry : map.entrySet()) {
            jsonObject.add(Registry.STATS.method_19959((class_4474<?>)entry.getKey()).toString(), (JsonElement)entry.getValue());
        }
        return jsonObject;
    }
}
