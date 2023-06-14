package com.redlimerl.speedrunigt.mixins.keybinding;

import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.utils.KeyBindingRegistry;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
    @Mutable @Shadow public KeyBinding[] allKeys;

    @Inject(at = @At("HEAD"), method = "load()V")
    public void loadHook(CallbackInfo info) {
        // Key Bindings initialize
        SpeedRunIGTClient.timerResetKeyBinding = KeyBindingRegistry.registerKeyBinding(new KeyBinding(
                "speedrunigt.controls.start_timer",
                22,
                "speedrunigt.title.options"
        ));
        SpeedRunIGTClient.timerStopKeyBinding = KeyBindingRegistry.registerKeyBinding(new KeyBinding(
                "speedrunigt.controls.stop_timer",
                23,
                "speedrunigt.title.options"
        ));
        allKeys = KeyBindingRegistry.process(allKeys);
    }
}
