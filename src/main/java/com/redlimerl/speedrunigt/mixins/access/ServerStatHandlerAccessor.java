package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.stat.ServerStatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerStatHandler.class)
public interface ServerStatHandlerAccessor {
    @Invoker("asString") String invokeAsString();
}
