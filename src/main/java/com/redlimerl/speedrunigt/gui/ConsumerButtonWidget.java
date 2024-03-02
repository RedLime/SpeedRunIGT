package com.redlimerl.speedrunigt.gui;

import net.minecraft.client.gui.widget.ClickableWidget;

public class ConsumerButtonWidget extends ClickableWidget {

    public interface ButtonWorker {
        void accept(ClickableWidget buttonWidget);
    }
    private final ButtonWorker onClick;

    public ConsumerButtonWidget(int x, int y, int width, int height, String message, ButtonWorker onClick) {
        super(0, x, y, width, height, message);
        this.onClick = onClick;
    }

    public void onClick() {
        onClick.accept(this);
    }
}