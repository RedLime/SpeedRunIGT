package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.client.gui.widget.AbstractButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractButtonWidget.class)
public interface AbstractButtonWidgetAccessor {
    @Accessor("height")
    int getHeight();
}
