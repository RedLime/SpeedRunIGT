package com.redlimerl.speedrunigt.mixins.access;

import com.google.gson.JsonElement;
import net.minecraft.stats.ServerStatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerStatsCounter.class)
public interface ServerStatsCounterAccessor {

    @Invoker("toJson")
    JsonElement invokeToJson();

}
