package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.ColorHelper;

import java.util.Locale;

public class FailedCategoryInitScreen extends Screen {
    private final String fileName;
    private final InvalidCategoryException exception;

    // Stat, Advancement, Kill, Obtain Item

    public FailedCategoryInitScreen(String fileName, InvalidCategoryException exception) {
        super(new LiteralText(""));
        this.fileName = fileName;
        this.exception = exception;
        exception.printStackTrace();
        SpeedRunIGT.error(String.format("Failed to add %s, because of %s", fileName, exception));
        if (!exception.getDetails().isEmpty()) SpeedRunIGT.error(String.format("Details : %s", exception.getDetails()));
    }

    @Override
    protected void init() {
        addDrawableChild(new ButtonWidget(width / 2 - 100, height / 2 + 15, 200, 20, ScreenTexts.DONE, button -> onClose()));
    }

    private static final int TEXT_WHITE = ColorHelper.Argb.getArgb(255, 255, 255, 255);
    private static final int TEXT_RED = ColorHelper.Argb.getArgb(255, 255, 70, 70);
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, new TranslatableText("speedrunigt.message.failed_add_category", this.fileName), width / 2, height / 2 - 35, TEXT_RED);
        drawCenteredText(matrices, this.textRenderer, new TranslatableText("speedrunigt.message.failed_add_category."+exception.getReason().name().toLowerCase(Locale.ROOT)), width / 2, height / 2 - 10, TEXT_WHITE);
        drawCenteredText(matrices, this.textRenderer, exception.getDetails(), width / 2, height / 2 + 2, TEXT_WHITE);
    }
}
