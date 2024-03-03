package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.instance.GameInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.SeedCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SeedCommand.class)
public class SeedCommandMixin {
    @Inject(method = "method_0_5962", at = @At("TAIL"))
    private void seenSeed(MinecraftServer minecraftServer, CommandOutput commandOutput, String[] strings, CallbackInfo ci) {
        GameInstance.getInstance().callEvents("view_seed");
    }
}
