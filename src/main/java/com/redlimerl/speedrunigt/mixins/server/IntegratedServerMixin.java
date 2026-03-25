package com.redlimerl.speedrunigt.mixins.server;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {

    @Inject(method = "publishServer", at = @At("RETURN"))
    public void onOpenLan(GameType gameMode, boolean cheatsAllowed, int port, CallbackInfoReturnable<Boolean> cir) {
        if (InGameTimer.getInstance().getStatus() != TimerStatus.NONE) {
            InGameTimer.getInstance().openedLanIntegratedServer();
            if(cheatsAllowed){
                InGameTimer.getInstance().setCheatAvailable(true);
            }
        }
    }

}
