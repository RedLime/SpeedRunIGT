package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.JsonIntSerializable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(StatHandler.class)
public interface ServerStatHandlerAccessor {

    @Accessor("field_9047")
    Map<Stat, JsonIntSerializable> getStatMap();

}
