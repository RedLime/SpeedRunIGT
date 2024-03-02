package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerClientUtils;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.class_3525;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {

    @Shadow @Final private static class_3525<KeyBinding> field_1658;

    @Inject(method = "method_1416", at = @At("TAIL"))
    private static void onPress(int keyCode, boolean pressed, CallbackInfo ci) {
        KeyBinding keyBinding =field_1658.method_15316(keyCode);
        if(keyBinding!=null && pressed){
            InGameTimer timer = InGameTimer.getInstance();
            if (InGameTimerClientUtils.isFocusedClick() &&
                    (keyBinding == MinecraftClient.getInstance().options.advancementsKey
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
