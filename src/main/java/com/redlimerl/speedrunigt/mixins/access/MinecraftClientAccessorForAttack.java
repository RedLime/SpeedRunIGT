package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessorForAttack {

    @Accessor("attackCooldown")
    int getAttackCoolDown();

}
