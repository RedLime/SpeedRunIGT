package com.redlimerl.speedrunigt.mixins.screen;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.options.ControlsOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ControlsOptionsScreen.class)
public abstract class ControlsOptionsScreenMixin extends Screen {
    @ModifyArg(method = "init", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;<init>(IIILjava/lang/String;)V"))
    private int dynamicDoneButton(int num) {
        return !FabricLoader.getInstance().isModLoaded("legacy-fabric-api") ? this.height / 6 + 24 * ((this.client.options.allKeys.length + 2 - 1) / 2) : num;
    }
}
