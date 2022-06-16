package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {

    @Shadow private TextFieldWidget seedField;

    @Inject(method = "buttonClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;startGame(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/world/level/LevelInfo;)V", shift = At.Shift.BEFORE))
    public void onGenerate(CallbackInfo ci) {
        InGameTimerUtils.IS_SET_SEED = !StringUtils.isEmpty(this.seedField.getText()) || isAtumSetSeed();
    }

    private boolean isAtumSetSeed() {
        boolean isRunning = false, isSetSeed = false;
        try {
            Class<?> refClass = Class.forName("me.voidxwalker.autoreset.Atum");
            for (Field field : refClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    if (field.getName().equals("seed")) {
                        String seed = (String) field.get(null);
                        if (!StringUtils.isEmpty(seed)) isSetSeed = true;
                        else break;
                    }
                    if (field.getName().equals("isRunning")) {
                        boolean running = (boolean) field.get(null);
                        if (running) isRunning = true;
                        else break;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return isRunning && isSetSeed;
    }


}
