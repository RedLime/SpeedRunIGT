package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftClientAccessorForAttack {

    @Accessor("attackCooldown")
    int getAttackCoolDown();

    @Accessor
    boolean isPaused();
}
