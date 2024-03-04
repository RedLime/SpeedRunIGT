package com.redlimerl.speedrunigt.mixins.keybinding;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import net.minecraft.client.option.KeyBinding;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor("categories")
    static Set<String> invokeGetCategoryMap() {
        throw new AssertionError();
    }
}
