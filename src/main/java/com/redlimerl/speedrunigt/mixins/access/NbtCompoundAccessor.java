package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(NbtCompound.class)
public interface NbtCompoundAccessor {
    @Accessor("data")
    Map<String, Object> getData();
}
