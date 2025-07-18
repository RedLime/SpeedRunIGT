package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.structure.StructureFeature;
import net.minecraft.world.gen.GeneratorConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureFeature.class)
public interface StructureFeatureAccessor {
    @Invoker("method_5516")
    GeneratorConfig invokeMethod_5516(int x, int y, int z);
}
