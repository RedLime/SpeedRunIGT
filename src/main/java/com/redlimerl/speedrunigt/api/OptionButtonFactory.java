package com.redlimerl.speedrunigt.api;

import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.mixins.access.ButtonWidgetAccessor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import java.util.function.Supplier;

public interface OptionButtonFactory {
    class Builder {
        private String category = null;
        private ClickableWidget buttonWidget = new ConsumerButtonWidget(0, 0, 150, 20, "", (button) -> {});
        private Supplier<String> tooltip = null;

        public Builder setButtonWidget(ClickableWidget buttonWidget) {
            ButtonWidgetAccessor buttonWidgetAccessor = (ButtonWidgetAccessor) buttonWidget;
            if (buttonWidget.method_1825() != 150 || buttonWidgetAccessor.getHeight() != 20) throw new IllegalArgumentException("ButtonWidget should be width 150, height 20");
            if (buttonWidget.getClass().getTypeName().equals(ClickableWidget.class.getTypeName())) throw new IllegalArgumentException("yeah you can use ButtonWidget extends class, but you can't use ButtonWidget. try use ConsumerButtonWidget.");
            this.buttonWidget = buttonWidget;
            return this;
        }

        public Builder setToolTip(Supplier<String> toolTipSupplier) {
            this.tooltip = toolTipSupplier;
            return this;
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Storage build() {
            return new Storage(category, buttonWidget, tooltip);
        }
    }

    class Storage {
        private final String category;
        private final ClickableWidget buttonWidget;
        private final Supplier<String> tooltip;
        public Storage(String category, ClickableWidget buttonWidget, Supplier<String> tooltip) {
            this.category = category;
            this.buttonWidget = buttonWidget;
            this.tooltip = tooltip;
        }

        public String getCategory() {
            return category;
        }

        public ClickableWidget getButtonWidget() {
            return buttonWidget;
        }

        public Supplier<String> getTooltip() {
            return tooltip;
        }
    }

    Builder create(Screen screen);
}
