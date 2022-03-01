package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(LevelStorage.class)
public interface LevelStorageAccessor {
    @Accessor("file")
    File getFile();
}
