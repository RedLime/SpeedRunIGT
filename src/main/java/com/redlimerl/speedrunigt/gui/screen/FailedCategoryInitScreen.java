package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import com.redlimerl.speedrunigt.version.ColorMixer;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.TranslatableTextContent;
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
    public void method_2224() {
        field_2564.add(new ConsumerButtonWidget(field_2561 / 2 - 100, field_2559 / 2 + 15, 200, 20, ScreenTexts.DONE, button -> MinecraftClient.getInstance().setScreen(null)));
    }

    @Override
    protected void method_0_2778(ClickableWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.method_0_2778(button);
    }

    private static final int TEXT_WHITE = ColorMixer.getArgb(255, 255, 255, 255);
    private static final int TEXT_RED = ColorMixer.getArgb(255, 255, 70, 70);

    @Override
    public void method_2214(int mouseX, int mouseY, float delta) {
        method_2240();
        super.method_2214(mouseX, mouseY, delta);
        method_1789(this.field_2554, new TranslatableTextContent("speedrunigt.message.failed_add_category", this.fileName).method_10865(), field_2561 / 2, field_2559 / 2 - 35, TEXT_RED);
        method_1789(this.field_2554, new TranslatableTextContent("speedrunigt.message.failed_add_category."+exception.getReason().name().toLowerCase(Locale.ROOT)).method_10865(), field_2561 / 2, field_2559 / 2 - 10, TEXT_WHITE);
        method_1789(this.field_2554, exception.getDetails(), field_2561 / 2, field_2559 / 2 + 2, TEXT_WHITE);
    }
}
