package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.PracticeTimerManager;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "setOverlayMessage(Ljava/lang/String;Z)V", at = @At("HEAD"))
    public void onTitleMixin(String string, boolean tinted, CallbackInfo ci) {
        if (!SpeedRunOption.getOption(SpeedRunOptions.ENABLE_PRACTICE_DETECT))
            return;
        if ((InGameTimer.getInstance().isStarted() && !InGameTimer.getInstance().isCompleted()) && InGameTimer.getInstance().getCategory() == PracticeTimerManager.PRACTICE_CATEGORY)
            return;

        Float time = null;

        if (string.contains(":")) {
            Matcher matcher = Pattern.compile("(\\d+):\\d+(:\\d+)?").matcher(string);
            if (matcher.find()) {
                PracticeTimerManager.startPractice(0);
                return;
            }
        } else {
            try {
                time = Float.parseFloat(string);
            } catch (NumberFormatException ignored) {
            }
        }

        if (time != null) {
            PracticeTimerManager.startPractice(time);
        }
    }
}
