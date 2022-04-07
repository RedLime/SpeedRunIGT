package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import com.redlimerl.speedrunigt.version.ColorMixer;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.TranslatableText;

import java.util.Locale;

public class FailedCategoryInitScreen extends Screen {
    private final String fileName;
    private final InvalidCategoryException exception;

    // Stat, Advancement, Kill, Obtain Item

    public FailedCategoryInitScreen(String fileName, InvalidCategoryException exception) {
        super();
        this.fileName = fileName;
        this.exception = exception;
        exception.printStackTrace();
        SpeedRunIGT.error(String.format("Failed to add %s, because of %s", fileName, exception));
        if (!exception.getDetails().isEmpty()) SpeedRunIGT.error(String.format("Details : %s", exception.getDetails()));
    }

    @Override
    public void init() {
        buttons.add(new ConsumerButtonWidget(width / 2 - 100, height / 2 + 15, 200, 20, ScreenTexts.DONE, button -> MinecraftClient.getInstance().openScreen(null)));
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.buttonClicked(button);
    }

    private static final int TEXT_WHITE = ColorMixer.getArgb(255, 255, 255, 255);
    private static final int TEXT_RED = ColorMixer.getArgb(255, 255, 70, 70);

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        renderBackground();
        super.render(mouseX, mouseY, delta);
        drawCenteredString(this.textRenderer, new TranslatableText("speedrunigt.message.failed_add_category", this.fileName).asFormattedString(), width / 2, height / 2 - 35, TEXT_RED);
        drawCenteredString(this.textRenderer, new TranslatableText("speedrunigt.message.failed_add_category."+exception.getReason().name().toLowerCase(Locale.ROOT)).asFormattedString(), width / 2, height / 2 - 10, TEXT_WHITE);
        drawCenteredString(this.textRenderer, exception.getDetails(), width / 2, height / 2 + 2, TEXT_WHITE);
    }
}
