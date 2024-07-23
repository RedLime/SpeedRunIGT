package com.redlimerl.speedrunigt.mixins.server;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {

    @Inject(method = "getPort", at = @At("RETURN"))
    public void onOpenLan(LevelInfo.GameMode gamemode, boolean bl, CallbackInfoReturnable<String> cir) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) {
            InGameTimer.getInstance().openedLanIntegratedServer();
            if(bl){
                InGameTimer.getInstance().setCheatAvailable(true);
            }
        }
    }

}
