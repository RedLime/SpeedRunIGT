package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.mixins.access.MinecraftClientAccessor;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
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

    @Shadow @Final private static Map<InputUtil.Key, KeyBinding> keyToBindings;

    @Inject(method = "setKeyPressed", at = @At("TAIL"))
    private static void onPress(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        KeyBinding keyBinding = keyToBindings.get(key);
        if (timer.getStatus() == TimerStatus.NONE || timer.getStatus() == TimerStatus.COMPLETED_LEGACY) return;
        if (keyBinding != null && pressed) {
            if (keyBinding == MinecraftClient.getInstance().options.keyAdvancements // Advancement
                    || Objects.equals(keyBinding.getCategory(), "key.categories.inventory")
                    || (((MinecraftClientAccessor) MinecraftClient.getInstance()).getAttackCooldown() != 10000 && Objects.equals(keyBinding.getCategory(), "key.categories.gameplay"))) {
                if (timer.getStatus() == TimerStatus.IDLE && InGameTimer.checkingWorld) {
                    timer.setPause(false);
                }
                timer.updateFirstInput();
            }
            if (keyBinding == SpeedRunIGT.timerResetKeyBinding) {
                if (timer.getCategory() == RunCategories.CUSTOM && timer.isResettable()) {
                    InGameTimer.reset();
                }
            }
            if (keyBinding == SpeedRunIGT.timerStopKeyBinding) {
                if (timer.getCategory() == RunCategories.CUSTOM && timer.isStarted()) {
                    InGameTimer.complete();
                }
            }
        }
    }
}
