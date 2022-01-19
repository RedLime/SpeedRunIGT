package com.redlimerl.speedrunigt.mixins.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.utils.DataUtils;
import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.world.gen.GeneratorOptions;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalLong;

@Mixin(MoreOptionsDialog.class)
public abstract class MoreOptionsDialogMixin {

    @Shadow private TextFieldWidget seedTextField;

    @Inject(method = "getGeneratorOptions", at = @At("HEAD"))
    public void onLoadOptions(boolean hardcore, CallbackInfoReturnable<GeneratorOptions> cir) {
        String string = this.seedTextField.getText();
        if (!StringUtils.isEmpty(string)) {
            OptionalLong seedParse = DataUtils.tryParseLong(string);
            SpeedRunIGT.LATEST_IS_SSG = true;
            if (seedParse.isPresent()) {
                SpeedRunIGT.LATEST_PLAYED_SEED = seedParse.getAsLong();
            } else {
                OptionalLong textSeed = OptionalLong.of(string.hashCode());
                SpeedRunIGT.LATEST_PLAYED_SEED = textSeed.getAsLong();
            }
            return;
        }

        SpeedRunIGT.LATEST_IS_SSG = false;
    }
}
