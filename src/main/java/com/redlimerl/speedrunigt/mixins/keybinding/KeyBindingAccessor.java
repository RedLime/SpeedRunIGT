package com.redlimerl.speedrunigt.mixins.keybinding;

import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor("categories")
    static Set<String> invokeGetCategoryMap() {
        throw new AssertionError();
    }
}
