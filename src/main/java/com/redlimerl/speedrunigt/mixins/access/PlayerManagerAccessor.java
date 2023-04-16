package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerManager.class)
public interface PlayerManagerAccessor {

    @Accessor("cheatsAllowed")
    boolean isCheatsAllowedInject();

}
