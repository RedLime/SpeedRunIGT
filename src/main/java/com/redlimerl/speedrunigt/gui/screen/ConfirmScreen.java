package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.gui.IdentifiableBooleanConsumer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;
import net.minecraft.util.Language;

public class ConfirmScreen extends Screen {
    protected IdentifiableBooleanConsumer consumer;
    protected String title;
    private final String subtitle;
    protected String yesText;
    protected String noText;
    protected int identifier;
    private int buttonEnableTimer;

    public ConfirmScreen(IdentifiableBooleanConsumer identifiableBooleanConsumer, String string, String string2, int i) {
        this.consumer = identifiableBooleanConsumer;
        this.title = string;
        this.subtitle = string2;
        this.identifier = i;
        this.yesText = Language.getInstance().translate("gui.yes");
        this.noText = Language.getInstance().translate("gui.no");
    }

    public ConfirmScreen(IdentifiableBooleanConsumer identifiableBooleanConsumer, String string, String string2, String string3, String string4, int i) {
        this.consumer = identifiableBooleanConsumer;
        this.title = string;
        this.subtitle = string2;
        this.yesText = string3;
        this.noText = string4;
        this.identifier = i;
    }

    @Override
    public void init() {
        this.buttons.add(new OptionButtonWidget(0, this.width / 2 - 155, this.height / 6 + 96, this.yesText));
        this.buttons.add(new OptionButtonWidget(1, this.width / 2 - 155 + 160, this.height / 6 + 96, this.noText));
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        this.consumer.confirmResult(button.id == 0, this.identifier);
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        this.renderBackground();
        this.drawCenteredString(this.textRenderer, this.title, this.width / 2, 70, 16777215);
        this.drawCenteredString(this.textRenderer, this.subtitle, this.width / 2, 90, 16777215);
        super.render(mouseX, mouseY, tickDelta);
    }

    public void disableButtons(int duration) {
        this.buttonEnableTimer = duration;

        for (Object var3 : this.buttons) {
            ((ButtonWidget) var3).active = false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (--this.buttonEnableTimer == 0) {
            for (Object var2 : this.buttons) {
                ((ButtonWidget) var2).active = true;
            }
        }
    }
}
