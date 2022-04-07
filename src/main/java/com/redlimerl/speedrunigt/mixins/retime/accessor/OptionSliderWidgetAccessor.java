package com.redlimerl.speedrunigt.mixins.retime.accessor;

import net.minecraft.client.gui.widget.OptionSliderWidget;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionSliderWidget.class)
public interface OptionSliderWidgetAccessor {

    @Accessor("option")
    GameOptions.Option getOption();
}
