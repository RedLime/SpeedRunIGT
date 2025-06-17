package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.Locale;

import static com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker.*;

public class SpeedRunIGTInfoScreen extends Screen {

    private final Screen parent;

    private ButtonWidget update;

    public SpeedRunIGTInfoScreen(Screen parent) {
        super(Text.translatable("speedrunigt.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        checkUpdate();
        assert client != null;
        update = addDrawableChild(ButtonWidgetHelper.create(width / 2 - 155, height - 104, 150, 20, Text.translatable("speedrunigt.menu.download_update"), (ButtonWidget button) -> Util.getOperatingSystem().open(UPDATE_URL)));
        update.active = false;
        addDrawableChild(ButtonWidgetHelper.create(width / 2 + 5, height - 104, 150, 20, Text.translatable("speedrunigt.menu.latest_change_log"), (ButtonWidget button) -> Util.getOperatingSystem().open("https://github.com/RedLime/SpeedRunIGT/releases/latest")));

        addDrawableChild(ButtonWidgetHelper.create(width / 2 - 155, height - 80, 150, 20, Text.translatable("speedrunigt.menu.open_github_repo"), (ButtonWidget button) -> Util.getOperatingSystem().open("https://github.com/RedLime/SpeedRunIGT/")));
        addDrawableChild(ButtonWidgetHelper.create(width / 2 + 5, height - 80, 150, 20, Text.translatable("speedrunigt.menu.open_support_page"), (ButtonWidget button) -> Util.getOperatingSystem().open("https://ko-fi.com/redlimerl")));
        addDrawableChild(ButtonWidgetHelper.create(width / 2 - 100, height - 40, 200, 20, ScreenTexts.BACK, (ButtonWidget button) -> client.setScreen(parent)));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(1.5F, 1.5F);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 3, 15, Colors.WHITE);
        context.getMatrices().popMatrix();

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Version : "+ SpeedRunIGT.MOD_VERSION.split("\\+")[0]), this.width / 2, 78, Colors.WHITE);
        if (UPDATE_STATUS != UpdateStatus.NONE) {
            if (UPDATE_STATUS == UpdateStatus.OUTDATED) {
                update.active = true;
                context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Updated Version : "+ UPDATE_VERSION).formatted(Formatting.YELLOW), this.width / 2, 88, Colors.WHITE);
            }
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("speedrunigt.message.update."+UPDATE_STATUS.name().toLowerCase(Locale.ROOT)),
                    this.width / 2, 116, Colors.WHITE);
        }
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
