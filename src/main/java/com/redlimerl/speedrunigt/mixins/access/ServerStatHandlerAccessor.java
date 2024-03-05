package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.stat.StatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(StatHandler.class)
public interface ServerStatHandlerAccessor {

    @Accessor("field_2280")
    Map getStatMap();

}
