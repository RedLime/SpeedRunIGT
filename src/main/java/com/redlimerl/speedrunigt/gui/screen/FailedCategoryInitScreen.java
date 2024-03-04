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
    public void method_21947() {
        field_22537.add(new ConsumerButtonWidget(field_22535 / 2 - 100, field_22536 / 2 + 15, 200, 20, ScreenTexts.DONE, button -> MinecraftClient.getInstance().setScreen(null)));
    }

    @Override
    protected void method_21930(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.method_21930(button);
    }

    private static final int TEXT_WHITE = ColorMixer.getArgb(255, 255, 255, 255);
    private static final int TEXT_RED = ColorMixer.getArgb(255, 255, 70, 70);

    @Override
    public void method_21925(int mouseX, int mouseY, float delta) {
        method_21946();
        super.method_21925(mouseX, mouseY, delta);
        method_21881(this.field_22540, new TranslatableText("speedrunigt.message.failed_add_category", this.fileName).asFormattedString(), field_22535 / 2, field_22536 / 2 - 35, TEXT_RED);
        method_21881(this.field_22540, new TranslatableText("speedrunigt.message.failed_add_category."+exception.getReason().name().toLowerCase(Locale.ROOT)).asFormattedString(), field_22535 / 2, field_22536 / 2 - 10, TEXT_WHITE);
        method_21881(this.field_22540, exception.getDetails(), field_22535 / 2, field_22536 / 2 + 2, TEXT_WHITE);
    }
}
