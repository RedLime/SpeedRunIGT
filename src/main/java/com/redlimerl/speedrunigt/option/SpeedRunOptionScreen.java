package com.redlimerl.speedrunigt.option;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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

    static HashMap<Element, List<Text>> tooltips = new HashMap<>();
    @Override
    protected void init() {
        super.init();

        int buttonCount = 0;
        for (Function<Screen, ClickableWidget> function : SpeedRunOptions.buttons.subList(page*12, Math.min(SpeedRunOptions.buttons.size(), (page + 1) * 12))) {
            ClickableWidget button = function.apply(this);
            tooltips.put(button, SpeedRunOptions.tooltips.get(function));

            if (button.getWidth() != 150 || button.getHeight() != 20)
                throw new IllegalArgumentException("Only can be set to width to 150 and height to 20");

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

        SpeedRunIGTInfoScreen.checkUpdate();
    }

    @Override
    public void onClose() {
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
        super.render(matrices, mouseX, mouseY, delta);

        if (SpeedRunIGTInfoScreen.UPDATE_STATUS == SpeedRunIGTInfoScreen.UpdateStatus.OUTDATED) {
            drawCenteredText(matrices, this.textRenderer, new TranslatableText("speedrunigt.message.update_found"), this.width / 2,  this.height - 48, 16777215);
        }

        Optional<Element> e = this.hoveredElement(mouseX, mouseY);
        if (e.isPresent()) {
            if (!tooltips.containsKey(e.get())) return;

            ArrayList<Text> tts = new ArrayList<>();
            for (Text text : tooltips.get(e.get())) {
                for (String s : text.getString().split("\n")) {
                    tts.add(new LiteralText(s));
                }
            }
            if (!tts.isEmpty()) this.renderTooltip(matrices, tts, mouseX, mouseY);
        }
    }
}
