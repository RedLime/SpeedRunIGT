package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.instance.GameInstance;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.SeedCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SeedCommand.class)
public class SeedCommandMixin {
    @Inject(method = "execute", at = @At("TAIL"))
    private void seenSeed(CommandSource source, String[] args, CallbackInfo ci) {
        GameInstance.getInstance().callEvents("view_seed");
    }
}
