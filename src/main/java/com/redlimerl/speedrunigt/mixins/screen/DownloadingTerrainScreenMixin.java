package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DownloadingTerrainScreen.class)
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

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen;drawCenteredText(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"), index = 2)
    public Text onRender(Text string) {
        if (InGameTimer.getInstance().isPaused() && InGameTimer.getInstance().isStarted() && !InGameTimer.getInstance().isCoop()) {
            return new LiteralText(string.getString() + " (#" + InGameTimer.getInstance().getPauseCount() + ")");
        } else {
            return string;
        }
    }
}
