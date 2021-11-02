package com.redlimerl.speedrunigt.option;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class SpeedRunOptionScreen extends Screen {

    private final Screen parent;
    private final int page;

    public SpeedRunOptionScreen(Screen parent) {
        this(parent, 0);
    }

    public SpeedRunOptionScreen(Screen parent, int page) {
        super(new TranslatableText("speedrunigt.title.options"));
        this.page = page;
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
        SpeedRunOptions.addOptionButton(
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.any_percent_mode").append(": ").append(
                                SpeedRunOptions.getOption(SpeedRunOptions.ANY_PERCENT_MODE) ? ScreenTexts.ON : ScreenTexts.OFF)
                        , (ButtonWidget button) -> {
                    SpeedRunOptions.setOption(SpeedRunOptions.ANY_PERCENT_MODE, !SpeedRunOptions.getOption(SpeedRunOptions.ANY_PERCENT_MODE));
                    button.setMessage(new TranslatableText("speedrunigt.option.any_percent_mode").append(": ").append(
                            SpeedRunOptions.getOption(SpeedRunOptions.ANY_PERCENT_MODE) ? ScreenTexts.ON : ScreenTexts.OFF));
                }), new TranslatableText("speedrunigt.option.any_percent_mode.description")
        );
    }

    @Override
    protected void init() {
        super.init();

        int buttonCount = 0;
        for (ClickableWidget button : SpeedRunOptions.buttons.subList(page*12, Math.min(SpeedRunOptions.buttons.size(), (page + 1) * 12))) {
            button.x = width / 2 - 155 + buttonCount % 2 * 160;
            button.y = height / 6 - 12 + 24 * (buttonCount / 2);
            addDrawableChild(button);
            buttonCount++;
        }

        addDrawableChild(new ButtonWidget(width / 2 - 100, height / 6 + 168, 200, 20, ScreenTexts.DONE, (ButtonWidget button) -> {
            if (client != null) client.setScreen(parent);
        }));

        if (SpeedRunOptions.buttons.size() > 12) {
            ButtonWidget nextButton = addDrawableChild(new ButtonWidget(width / 2 - 155 + 260, height / 6 + 144, 50, 20, new LiteralText(">>>"),
                    (ButtonWidget button) -> {
                        if (client != null) client.setScreen(new SpeedRunOptionScreen(parent, page + 1));
                    }));
            ButtonWidget prevButton = addDrawableChild(new ButtonWidget(width / 2 - 155, height / 6 + 144, 50, 20, new LiteralText("<<<"),
                    (ButtonWidget button) -> {
                        if (client != null) client.setScreen(new SpeedRunOptionScreen(parent, page - 1));
                    }));
            if ((SpeedRunOptions.buttons.size() - 1) / 12 == page) {
                nextButton.active = false;
            }
            if (page == 0) {
                prevButton.active = false;
            }
        }
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

        Optional<Element> e = this.hoveredElement(mouseX, mouseY);
        if (e.isPresent()) {
            List<Text> tooltips = SpeedRunOptions.tooltips.get(e.get());
            if (tooltips != null && !tooltips.isEmpty()) this.renderTooltip(matrices, tooltips, mouseX, mouseY);
        }
    }
}
