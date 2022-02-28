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

    @Override
    public void method_18374(double d, double e) {
        onClick.accept(this);
    }
}
