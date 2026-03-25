package com.redlimerl.speedrunigt.mixins.server;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin {

    @Inject(method = "initServer", at = @At("RETURN"))
    private void onCreate(CallbackInfoReturnable<Boolean> cir) {
        SpeedRunIGT.DEDICATED_SERVER = ((MinecraftServer) (Object) this);
    }
}
