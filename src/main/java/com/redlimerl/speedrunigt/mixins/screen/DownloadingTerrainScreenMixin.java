package com.redlimerl.speedrunigt.mixins.screen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelLoadingScreen.class)
public abstract class DownloadingTerrainScreenMixin extends Screen {

    protected DownloadingTerrainScreenMixin(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        InGameTimer timer = InGameTimer.getInstance();
        if (this.minecraft != null && this.minecraft.isLocalServer() && !timer.isCoop() && timer.getStatus() != TimerStatus.IDLE) {
            timer.setPause(true, TimerStatus.IDLE, "dimension load?");
            InGameTimerUtils.IS_CHANGING_DIMENSION = false;
        }
    }

    @WrapOperation(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/LevelLoadingScreen;DOWNLOADING_TERRAIN_TEXT:Lnet/minecraft/network/chat/Component;"))
    public Component onRender(Operation<Component> original) {
        if (InGameTimer.getInstance().isPaused() && InGameTimer.getInstance().isStarted() && !InGameTimer.getInstance().isCoop()) {
            return Component.literal(original.call().getString() + " (#" + InGameTimer.getInstance().getPauseCount() + ")");
        } else {
            return original.call();
        }
    }
}
