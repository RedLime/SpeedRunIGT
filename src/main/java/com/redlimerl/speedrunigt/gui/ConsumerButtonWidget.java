package com.redlimerl.speedrunigt.gui;

import net.minecraft.client.gui.widget.ButtonWidget;

public class ConsumerButtonWidget extends ButtonWidget {

    public interface ButtonWorker {
        void accept(ButtonWidget buttonWidget);
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