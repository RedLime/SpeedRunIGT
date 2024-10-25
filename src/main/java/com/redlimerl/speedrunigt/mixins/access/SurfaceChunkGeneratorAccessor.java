package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.structure.StrongholdStructure;
import net.minecraft.world.chunk.SurfaceChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SurfaceChunkGenerator.class)
public interface SurfaceChunkGeneratorAccessor {

    @Accessor("strongholdGenerator")
    StrongholdStructure getStrongholdGenerator();

}
