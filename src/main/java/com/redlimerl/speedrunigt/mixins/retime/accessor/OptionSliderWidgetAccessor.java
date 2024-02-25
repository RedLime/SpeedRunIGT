package com.redlimerl.speedrunigt.mixins.retime.accessor;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SliderWidget.class)
public interface OptionSliderWidgetAccessor {

    @Accessor("field_2162")
    GameOptions.class_316 getOption();
}
