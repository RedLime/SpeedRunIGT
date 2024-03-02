package com.redlimerl.speedrunigt.mixins;

import com.mojang.brigadier.context.CommandContext;
import com.redlimerl.speedrunigt.instance.GameInstance;
import net.minecraft.server.command.SeedCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SeedCommand.class)
public class SeedCommandMixin {
    @Inject(method = "method_13617", at = @At("TAIL"))
    private static void seenSeed(CommandContext<ServerCommandSource> commandContext, CallbackInfoReturnable<Integer> cir) {
        GameInstance.getInstance().callEvents("view_seed");
    }
}
