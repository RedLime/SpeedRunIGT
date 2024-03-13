package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerClientUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.class_4107;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {

    @Shadow @Final private static Map<class_4107.class_4108, KeyBinding> field_19923;

    @Inject(method = "method_18168", at = @At("TAIL"))
    private static void onPress(class_4107.class_4108 key, boolean bl, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        KeyBinding keyBinding = field_19923.get(key);
        if (timer.getStatus() == TimerStatus.NONE || timer.getStatus() == TimerStatus.COMPLETED_LEGACY) return;
        if (keyBinding != null && bl) {
            if (InGameTimerClientUtils.isFocusedClick() &&
                    (keyBinding == MinecraftClient.getInstance().options.field_15880 // Advancement
                    || Objects.equals(keyBinding.getCategory(), "key.categories.inventory")
                    || Objects.equals(keyBinding.getCategory(), "key.categories.gameplay"))) {
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
