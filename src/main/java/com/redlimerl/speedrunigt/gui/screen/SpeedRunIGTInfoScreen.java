package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.Locale;

import static com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker.*;

public class SpeedRunIGTInfoScreen extends Screen {

    private final Screen parent;

    private ButtonWidget update;

    public SpeedRunIGTInfoScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        checkUpdate();
        assert client != null;
        update = addDrawableChild(new ButtonWidget(width / 2 - 155, height - 104, 150, 20, new TranslatableText("speedrunigt.menu.download_update"), (ButtonWidget button) -> Util.getOperatingSystem().open(UPDATE_URL)));
        update.active = false;
        addDrawableChild(new ButtonWidget(width / 2 + 5, height - 104, 150, 20, new TranslatableText("speedrunigt.menu.latest_change_log"), (ButtonWidget button) -> Util.getOperatingSystem().open("https://github.com/RedLime/SpeedRunIGT/releases/latest")));

        addDrawableChild(new ButtonWidget(width / 2 - 155, height - 80, 150, 20, new TranslatableText("speedrunigt.menu.open_github_repo"), (ButtonWidget button) -> Util.getOperatingSystem().open("https://github.com/RedLime/SpeedRunIGT/")));
        addDrawableChild(new ButtonWidget(width / 2 + 5, height - 80, 150, 20, new TranslatableText("speedrunigt.menu.open_support_page"), (ButtonWidget button) -> Util.getOperatingSystem().open("https://ko-fi.com/redlimerl")));
        addDrawableChild(new ButtonWidget(width / 2 - 100, height - 40, 200, 20, ScreenTexts.BACK, (ButtonWidget button) -> client.openScreen(parent)));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        matrices.push();
        matrices.scale(1.5F, 1.5F, 1.5F);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 3, 15, 16777215);
        matrices.pop();
        drawCenteredText(matrices, this.textRenderer, new LiteralText("Made by RedLime"), this.width / 2, 50, 16777215);
        drawCenteredText(matrices, this.textRenderer, new LiteralText("Discord : RedLime#0817"), this.width / 2, 62, 16777215);
        drawCenteredText(matrices, this.textRenderer,
                new LiteralText("Version : "+ SpeedRunIGT.MOD_VERSION.split("\\+")[0]), this.width / 2, 78, 16777215);
        if (UPDATE_STATUS != UpdateStatus.NONE) {
            if (UPDATE_STATUS == UpdateStatus.OUTDATED) {
                update.active = true;
                drawCenteredText(matrices, this.textRenderer, new LiteralText("Updated Version : "+ UPDATE_VERSION).formatted(Formatting.YELLOW), this.width / 2, 88, 16777215);
            }
            drawCenteredText(matrices, this.textRenderer,
                    new TranslatableText("speedrunigt.message.update."+UPDATE_STATUS.name().toLowerCase(Locale.ROOT)),
                    this.width / 2, 116, 16777215);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (client != null) {
            client.openScreen(parent);
        }
    }
}
