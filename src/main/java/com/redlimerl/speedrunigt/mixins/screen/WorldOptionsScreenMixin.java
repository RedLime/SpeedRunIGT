package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MoreOptionsDialog.class)
public class WorldOptionsScreenMixin {

    @Shadow private TextFieldWidget seedTextField;

    @Inject(method = "getGeneratorOptionsHolder", at = @At("HEAD"))
    public void onGenerate(CallbackInfoReturnable<GeneratorOptions> cir) {
        InGameTimerUtils.IS_SET_SEED = !this.seedTextField.getText().isEmpty();
    }
}
