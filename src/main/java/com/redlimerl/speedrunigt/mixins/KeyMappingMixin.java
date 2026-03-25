package com.redlimerl.speedrunigt.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerClientUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {

    @Shadow @Final private static Map<InputConstants.Key, List<KeyMapping>> MAP;

    @Inject(method = "set", at = @At("TAIL"))
    private static void onPress(InputConstants.Key key, boolean pressed, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        List<KeyMapping> keyBindings = MAP.get(key);
        if (timer.getStatus() == TimerStatus.NONE || timer.getStatus() == TimerStatus.COMPLETED_LEGACY || keyBindings == null) return;
        for (KeyMapping keyBinding : keyBindings) {
            if (keyBinding != null && pressed) {
                if (InGameTimerClientUtils.isFocusedClick() &&
                        (keyBinding == Minecraft.getInstance().options.keyAdvancements // Advancement
                                || keyBinding.getCategory().id().equals(Identifier.withDefaultNamespace("movement")))
                                || keyBinding.getCategory().id().equals(Identifier.withDefaultNamespace("gameplay"))) {
                    if (InGameTimerClientUtils.canUnpauseTimer(false)) {
                        timer.setPause(false, "pressed key");
                    }
                    timer.updateFirstInput();
                }
                if (keyBinding == SpeedRunIGTClient.timerResetKeyBinding) {
                    if (timer.getCategory() == RunCategories.CUSTOM && timer.isResettable()) {
                        InGameTimer.reset();
                    }
                }
                if (keyBinding == SpeedRunIGTClient.timerStopKeyBinding) {
                    if (timer.getCategory() == RunCategories.CUSTOM && timer.isStarted()) {
                        InGameTimer.complete();
                    }
                }
            }
        }
    }
}
