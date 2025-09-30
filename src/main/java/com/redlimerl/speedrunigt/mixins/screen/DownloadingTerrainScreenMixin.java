package com.redlimerl.speedrunigt.mixins.screen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelLoadingScreen.class)
public abstract class DownloadingTerrainScreenMixin extends Screen {

    protected DownloadingTerrainScreenMixin(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        InGameTimer timer = InGameTimer.getInstance();
        if (this.client != null && this.client.isInSingleplayer() && !timer.isCoop() && timer.getStatus() != TimerStatus.IDLE) {
            timer.setPause(true, TimerStatus.IDLE, "dimension load?");
            InGameTimerUtils.IS_CHANGING_DIMENSION = false;
        }
    }

    @WrapOperation(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/world/LevelLoadingScreen;DOWNLOADING_TERRAIN_TEXT:Lnet/minecraft/text/Text;"))
    public Text onRender(Operation<Text> original) {
        if (InGameTimer.getInstance().isPaused() && InGameTimer.getInstance().isStarted() && !InGameTimer.getInstance().isCoop()) {
            return Text.literal(original.call().getString() + " (#" + InGameTimer.getInstance().getPauseCount() + ")");
        } else {
            return original.call();
        }
    }
}
