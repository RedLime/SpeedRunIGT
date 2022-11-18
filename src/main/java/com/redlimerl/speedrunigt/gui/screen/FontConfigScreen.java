package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.utils.FontConfigure;
import com.redlimerl.speedrunigt.utils.FontIdentifier;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FontConfigScreen extends Screen {
    private final Screen parent;
    private final FontConfigure newFontConfigure;
    private final FontIdentifier fontIdentifier;

    protected FontConfigScreen(Screen parent, Identifier font) {
        super(new LiteralText("font_config"));
        this.parent = parent;
        this.fontIdentifier = SpeedRunIGT.FONT_MAPS.get(font);
        this.newFontConfigure = FontConfigure.fromJson(fontIdentifier.getFontConfigure().toString() + "");
    }

    @Override
    protected void init() {
        if (this.client == null) { return; }

        this.addButton(new ButtonWidget(this.width / 2 - 21, this.height / 2 - 45, 20, 20, new LiteralText("-"), button -> this.newFontConfigure.size = MathHelper.clamp(this.newFontConfigure.size - 1, 1, 50)));
        this.addButton(new ButtonWidget(this.width / 2 + 1, this.height / 2 - 45, 20, 20, new LiteralText("+"), button -> this.newFontConfigure.size = MathHelper.clamp(this.newFontConfigure.size + 1, 1, 50)));

        this.addButton(new ButtonWidget(this.width / 2 - 21, this.height / 2 + 5, 20, 20, new LiteralText("-"), button -> this.newFontConfigure.oversample = MathHelper.clamp(Math.round((this.newFontConfigure.oversample - (Screen.hasShiftDown() ? 1f : 0.1f)) * 10) / 10f, 0.1f, 20f)));
        this.addButton(new ButtonWidget(this.width / 2 + 1, this.height / 2 + 5, 20, 20, new LiteralText("+"), button -> this.newFontConfigure.oversample = MathHelper.clamp(Math.round((this.newFontConfigure.oversample + (Screen.hasShiftDown() ? 1f : 0.1f)) * 10) / 10f, 0.1f, 20f)));

        this.addButton(new ButtonWidget(this.width / 2 - 50, this.height - 62, 100, 20, new TranslatableText("speedrunigt.font.apply_and_save"), button -> {
            File config = SpeedRunIGT.FONT_PATH.resolve(this.fontIdentifier.getFile().getName().substring(0, this.fontIdentifier.getFile().getName().length() - 4) + ".json").toFile();
            try {
                FileUtils.writeStringToFile(config, this.newFontConfigure.toString(), StandardCharsets.UTF_8);
                this.client.reloadResources();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        this.addButton(new ButtonWidget(this.width / 2 - 50, this.height - 40, 100, 20, ScreenTexts.CANCEL, button -> this.onClose()));
    }

    @Override
    public void onClose() {
        if (this.client != null) this.client.openScreen(this.parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        this.drawCenteredText(matrices, this.textRenderer, new LiteralText("IGT: 01:23.456").setStyle(Style.EMPTY.withFont(this.fontIdentifier.getIdentifier())), this.width / 2, 30, 16777215);

        this.drawCenteredString(matrices, this.textRenderer, "§l" + I18n.translate("speedrunigt.font.size") + ": " + ((int) this.newFontConfigure.size), this.width / 2, this.height / 2 - 55, 16777215);
        this.drawCenteredString(matrices, this.textRenderer, "§l" + I18n.translate("speedrunigt.font.oversample") + ": " + this.newFontConfigure.oversample, this.width / 2, this.height / 2 - 5, 16777215);
        this.drawCenteredString(matrices, this.textRenderer, I18n.translate("speedrunigt.font.oversample.description"), this.width / 2, this.height / 2 + 27, 16777215);

        super.render(matrices, mouseX, mouseY, delta);
    }
}
