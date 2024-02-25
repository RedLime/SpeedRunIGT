package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.class_0_1685;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(StatHandler.class)
public interface ServerStatHandlerAccessor {

    @Accessor("field_9047")
    Map<Stat, class_0_1685> getStatMap();

}
