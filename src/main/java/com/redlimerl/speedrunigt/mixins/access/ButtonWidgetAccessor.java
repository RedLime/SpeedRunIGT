package com.redlimerl.speedrunigt.mixins.access;

import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClickableWidget.class)
public interface ButtonWidgetAccessor {
    @Accessor("height")
    int getHeight();
}
