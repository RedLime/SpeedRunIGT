package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.client.gui.widget.DoubleOptionSliderWidget;
import net.minecraft.client.option.DoubleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DoubleOptionSliderWidget.class)
public interface DoubleOptionSliderWidgetAccessor {

    @Accessor("option")
    DoubleOption getOption();

}
