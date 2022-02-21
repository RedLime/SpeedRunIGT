package com.redlimerl.speedrunigt.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ButtonScrollListWidget extends ElementListWidget<ButtonScrollListWidget.Entry> {

    public ButtonScrollListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
    }

    public void replaceButtons(Collection<AbstractButtonWidget> buttonWidgets) {
        ArrayList<Entry> list = new ArrayList<>();
        for (AbstractButtonWidget buttonWidget : buttonWidgets) {
            list.add(new Entry(buttonWidget));
        }
        replaceEntries(list);
    }

    @Override
    public int getRowWidth() {
        return 150;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
    }

    public class Entry extends ElementListWidget.Entry<ButtonScrollListWidget.Entry> {
        ArrayList<AbstractButtonWidget> children = new ArrayList<>();
        private final AbstractButtonWidget buttonWidget;

        public Entry(AbstractButtonWidget buttonWidget) {
            this.buttonWidget = buttonWidget;
            this.buttonWidget.x = (ButtonScrollListWidget.this.width - this.buttonWidget.getWidth()) / 2;
            children.add(this.buttonWidget);
        }

        @Override
        public List<? extends Element> children() {
            return children;
        }

        public AbstractButtonWidget getButtonWidget() {
            return buttonWidget;
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            buttonWidget.y = y;
            buttonWidget.render(matrices, mouseX, mouseY, tickDelta);
        }
    }
}
