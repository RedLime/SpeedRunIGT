package com.redlimerl.speedrunigt.mixins.retime.accessor;

import net.minecraft.class_350;
import net.minecraft.client.gui.widget.OptionSliderWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionSliderWidget.class)
public interface OptionSliderWidgetAccessor {

    @Accessor("field_7740")
    class_350 getOption();
}
