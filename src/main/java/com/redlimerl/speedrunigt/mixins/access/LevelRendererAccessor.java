package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {

    @Accessor("levelRenderState")
    LevelRenderState srigt$getLevelRenderState();

    @Invoker("countRenderedSections")
    int invokeCompletedChunkCount();

}
