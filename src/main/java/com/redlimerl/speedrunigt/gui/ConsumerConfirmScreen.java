package com.redlimerl.speedrunigt.gui;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;

import java.util.Objects;

public class ConsumerConfirmScreen extends ConfirmScreen {

    private final Worker worker;

    public interface Worker {
        void run(boolean b, int i);
    }

    public ConsumerConfirmScreen(Worker worker, String string, String string2, int i) {
        super(null, string, string2, i);
        this.worker = worker;
    }

    @Override
    protected void method_21930(ButtonWidget buttonWidget) {
        worker.run(Objects.equals(buttonWidget.field_22510, this.yesText), buttonWidget.id);
    }
}
