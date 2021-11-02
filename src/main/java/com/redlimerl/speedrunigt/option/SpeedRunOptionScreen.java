package com.redlimerl.speedrunigt.option;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

import java.util.Locale;

public class SpeedRunOptionScreen extends Screen {

    private final Screen parent;

    public SpeedRunOptionScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.title.options"));
        this.parent = parent;
    }

    static {
        SpeedRunOptions.buttons.add(
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.timer_position").append(": ").append(
                                new TranslatableText("speedrunigt.option.timer_position."+ SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POS).name().toLowerCase(Locale.ROOT))
                        ), (ButtonWidget button) -> {
                    SpeedRunOptions.setOption(SpeedRunOptions.TIMER_POS, getTimePosNext(SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POS)));
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position").append(": ").append(
                            new TranslatableText("speedrunigt.option.timer_position."+ SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POS).name().toLowerCase(Locale.ROOT))
                    ));
                }
                )
        );
        SpeedRunOptions.buttons.add(
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.timer_position").append(": ").append(
                                new TranslatableText("speedrunigt.option.timer_position."+ SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POS).name().toLowerCase(Locale.ROOT))
                        ), (ButtonWidget button) -> {
                    SpeedRunOptions.setOption(SpeedRunOptions.TIMER_POS, getTimePosNext(SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POS)));
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position").append(": ").append(
                            new TranslatableText("speedrunigt.option.timer_position."+ SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POS).name().toLowerCase(Locale.ROOT))
                    ));
                }
                )
        );
    }

    @Override
    protected void init() {
        super.init();

        int buttonCount = 0;
        for (ClickableWidget button : SpeedRunOptions.buttons) {
            button.x = width / 2 - 155 + buttonCount % 2 * 160;
            button.y = height / 6 - 12 + 24 * (buttonCount / 2);
            addDrawableChild(button);
            buttonCount++;
        }

        addDrawableChild(new ButtonWidget(width / 2 - 100, height / 6 + 168, 200, 20, ScreenTexts.DONE, (ButtonWidget button) -> {
            if (client != null) client.setScreen(parent);
        }));
    }

    @Override
    public void onClose() {
        if (this.client != null) this.client.setScreen(parent);
    }

    private static TimerPosition getTimePosNext(TimerPosition tp) {
        TimerPosition[] v = TimerPosition.values();
        return v[(tp.ordinal() + 1) % v.length];
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
