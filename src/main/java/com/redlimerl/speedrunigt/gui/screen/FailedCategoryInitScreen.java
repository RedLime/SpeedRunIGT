package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.Locale;

public class FailedCategoryInitScreen extends Screen {
    private final String fileName;
    private final InvalidCategoryException exception;

    // Stat, Advancement, Kill, Obtain Item

    public FailedCategoryInitScreen(String fileName, InvalidCategoryException exception) {
        super(Text.empty());
        this.fileName = fileName;
        this.exception = exception;
        exception.printStackTrace();
        SpeedRunIGT.error(String.format("Failed to add %s, because of %s", fileName, exception));
        if (!exception.getDetails().isEmpty()) SpeedRunIGT.error(String.format("Details : %s", exception.getDetails()));
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidgetHelper.create(width / 2 - 100, height / 2 + 15, 200, 20, ScreenTexts.DONE, button -> close()));
    }

    private static final int TEXT_WHITE = ColorHelper.getArgb(255, 255, 255, 255);
    private static final int TEXT_RED = ColorHelper.getArgb(255, 255, 70, 70);
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("speedrunigt.message.failed_add_category", this.fileName), width / 2, height / 2 - 35, TEXT_RED);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("speedrunigt.message.failed_add_category."+exception.getReason().name().toLowerCase(Locale.ROOT)), width / 2, height / 2 - 10, TEXT_WHITE);
        context.drawCenteredTextWithShadow(this.textRenderer, exception.getDetails(), width / 2, height / 2 + 2, TEXT_WHITE);
    }
}
