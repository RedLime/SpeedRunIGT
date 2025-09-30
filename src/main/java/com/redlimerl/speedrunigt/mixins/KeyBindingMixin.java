package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerClientUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {

    @Shadow @Final private static Map<InputUtil.Key, List<KeyBinding>> KEY_TO_BINDINGS;

    @Inject(method = "setKeyPressed", at = @At("TAIL"))
    private static void onPress(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        List<KeyBinding> keyBindings = KEY_TO_BINDINGS.get(key);
        if (timer.getStatus() == TimerStatus.NONE || timer.getStatus() == TimerStatus.COMPLETED_LEGACY || keyBindings == null) return;
        for (KeyBinding keyBinding : keyBindings) {
            if (keyBinding != null && pressed) {
                if (InGameTimerClientUtils.isFocusedClick() &&
                        (keyBinding == MinecraftClient.getInstance().options.advancementsKey // Advancement
                                || keyBinding.getCategory().id().equals(Identifier.ofVanilla("movement")))
                                || keyBinding.getCategory().id().equals(Identifier.ofVanilla("gameplay"))) {
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
