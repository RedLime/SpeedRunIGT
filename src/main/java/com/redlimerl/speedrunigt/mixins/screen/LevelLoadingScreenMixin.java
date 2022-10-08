package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.version.ColorMixer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LoadingScreenRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingScreenRenderer.class)
public class LevelLoadingScreenMixin {

    @Shadow private Window field_7695;

    @Shadow private MinecraftClient field_1029;

    @Inject(method = "setTitle", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        InGameTimerUtils.LATEST_TIMER_TIME = System.currentTimeMillis();
    }

    @Inject(method = "progressStagePercentage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;method_956(Ljava/lang/String;III)I", shift = At.Shift.BEFORE))
    public void onRender(int percentage, CallbackInfo ci) {
        long time = System.currentTimeMillis() - InGameTimerUtils.LATEST_TIMER_TIME;
        if (time < 2950) {
            GL11.glPushMatrix();
            String text = "SpeedRunIGT v" + (SpeedRunIGT.MOD_VERSION.split("\\+")[0]);
            this.field_1029.textRenderer.draw(text, (int) ((field_7695.getScaledWidth() - this.field_1029.textRenderer.getStringWidth(text)) / 2f), (int) field_7695.getScaledHeight() - 12,
                    ColorMixer.getArgb((int) (MathHelper.clamp((3000 - time) / 1000.0, 0, 1) * 90), 255, 255, 255));
            GL11.glPopMatrix();
        }
    }
}
