package com.redlimerl.speedrunigt.mixins;

import com.mojang.brigadier.context.CommandContext;
import com.redlimerl.speedrunigt.instance.GameInstance;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.SeedCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SeedCommand.class)
public class SeedCommandMixin {
    @Inject(method = "lambda$register$0", at = @At("TAIL"))
    private static void seenSeed(CommandContext<CommandSourceStack> commandContext, CallbackInfoReturnable<Integer> cir) {
        GameInstance.getInstance().callEvents("view_seed");
    }
}
