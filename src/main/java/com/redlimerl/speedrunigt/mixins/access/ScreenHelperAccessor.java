package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.class_4121;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_4121.class)
public interface ScreenHelperAccessor {

    @Invoker("method_18419")
    boolean accessIsHolding();
}
