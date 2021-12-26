package com.redlimerl.speedrunigt.mixins;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.util.IntObjectStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {


    @Shadow @Final private static IntObjectStorage<KeyBinding> KEY_MAP;
    /**
     * @author Void_X_Walker
     * @reason Backported to 1.8, redid custom keybinds
     */
    @Inject(method = "setKeyPressed", at = @At("TAIL"))
    private static void onPress(int keyCode, boolean pressed, CallbackInfo ci) {
        KeyBinding keyBinding =KEY_MAP.get(keyCode);
        if(keyBinding!=null){
            InGameTimer timer = InGameTimer.getInstance();
            if (keyBinding == MinecraftClient.getInstance().options.keySprint // Sprint
                    || Objects.equals(keyBinding.getCategory(), "key.categories.inventory")
                    || Objects.equals(keyBinding.getCategory(), "key.categories.gameplay")) {
                if (timer.getStatus() == TimerStatus.IDLE && InGameTimer.checkingWorld) {
                    timer.setPause(false);
                }
                timer.updateFirstInput();
            }
        }

    }
}
