package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
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
        for (Function<Screen, AbstractButtonWidget> function : SpeedRunOptions.buttons.subList(page*12, Math.min(SpeedRunOptions.buttons.size(), (page + 1) * 12))) {
            AbstractButtonWidget button = function.apply(this);
            tooltips.put(button, SpeedRunOptions.tooltips.get(function));

            button.x = width / 2 - 155 + buttonCount % 2 * 160;
            button.y = height / 6 - 12 + 24 * (buttonCount / 2);
            addButton(button);
            buttonCount++;
        }

        addButton(new ButtonWidget(width / 2 - 100, height / 6 + 168, 200, 20, ScreenTexts.DONE, (ButtonWidget button) -> {
            if (minecraft != null) minecraft.openScreen(parent);
        }));

        if (SpeedRunOptions.buttons.size() > 12) {
            ButtonWidget nextButton = addButton(new ButtonWidget(width / 2 - 155 + 260, height / 6 + 144, 50, 20,">>>",
                    (ButtonWidget button) -> {
                        if (minecraft != null) minecraft.openScreen(new SpeedRunOptionScreen(parent, page + 1));
                    }));
            ButtonWidget prevButton = addButton(new ButtonWidget(width / 2 - 155, height / 6 + 144, 50, 20, "<<<",
                    (ButtonWidget button) -> {
                        if (minecraft != null) minecraft.openScreen(new SpeedRunOptionScreen(parent, page - 1));
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
        if (this.minecraft != null) this.minecraft.openScreen(parent);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 15, 16777215);
        super.render(mouseX, mouseY, delta);

        if (SpeedRunIGTInfoScreen.UPDATE_STATUS == SpeedRunIGTInfoScreen.UpdateStatus.OUTDATED) {
            this.drawCenteredString(this.font, I18n.translate("speedrunigt.message.update_found"), this.width / 2,  this.height - 48, 16777215);
        }

        Optional<Element> e = this.hoveredElement(mouseX, mouseY);
        if (e.isPresent()) {
            if (!tooltips.containsKey(e.get())) return;

            ArrayList<String> tts = new ArrayList<>();
            for (Text text : tooltips.get(e.get())) {
                for (String s : text.getString().split("\n")) {
                    tts.add(new LiteralText(s).asFormattedString());
                }
            }
            if (!tts.isEmpty()) this.renderTooltip(tts, mouseX, mouseY);
        }
    }
}
