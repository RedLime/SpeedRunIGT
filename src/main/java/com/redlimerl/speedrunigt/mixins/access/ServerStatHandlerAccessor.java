package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.stat.ServerStatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerStatHandler.class)
public interface ServerStatHandlerAccessor {

    @Invoker("method_21417")
    String invokeAsString();

}
