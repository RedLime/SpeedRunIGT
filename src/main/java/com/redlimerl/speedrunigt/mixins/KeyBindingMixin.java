package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.util.collection.IntObjectStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {

    @Shadow @Final private static IntObjectStorage<KeyBinding> KEY_MAP;

    @Inject(method = "setKeyPressed", at = @At("TAIL"))
    private static void onPress(int keyCode, boolean pressed, CallbackInfo ci) {
        KeyBinding keyBinding =KEY_MAP.get(keyCode);
        if(keyBinding!=null && pressed){
            InGameTimer timer = InGameTimer.getInstance();
            if (Objects.equals(keyBinding.getCategory(), "key.categories.inventory")
                    || (Objects.equals(keyBinding.getCategory(), "key.categories.gameplay"))) {
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
