package com.redlimerl.speedrunigt.mixins.keybinding;

import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor("field_15867")
    static Map<String, Integer> invokeGetCategoryMap() {
        throw new AssertionError();
    }
}
