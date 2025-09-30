package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import com.redlimerl.speedrunigt.utils.FontConfigure;
import com.redlimerl.speedrunigt.utils.FontIdentifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
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
        super(Text.literal("font_config"));
        this.parent = parent;
        this.fontIdentifier = SpeedRunIGT.FONT_MAPS.get(font);
        this.newFontConfigure = FontConfigure.fromJson(fontIdentifier.getFontConfigure().toString());
    }

    @Override
    protected void init() {
        assert client != null;

        addDrawableChild(ButtonWidgetHelper.create(width / 2 - 21, height / 2 - 45, 20, 20, Text.literal("-"), button -> newFontConfigure.size = MathHelper.clamp(newFontConfigure.size - 1, 1, 50)));
        addDrawableChild(ButtonWidgetHelper.create(width / 2 + 1, height / 2 - 45, 20, 20, Text.literal("+"), button -> newFontConfigure.size = MathHelper.clamp(newFontConfigure.size + 1, 1, 50)));

        addDrawableChild(ButtonWidgetHelper.create(width / 2 - 21, height / 2 + 5, 20, 20, Text.literal("-"), button -> newFontConfigure.oversample = MathHelper.clamp(Math.round((newFontConfigure.oversample - (client.isShiftPressed() ? 1f : 0.1f)) * 10) / 10f, 0.1f, 20f)));
        addDrawableChild(ButtonWidgetHelper.create(width / 2 + 1, height / 2 + 5, 20, 20, Text.literal("+"), button -> newFontConfigure.oversample = MathHelper.clamp(Math.round((newFontConfigure.oversample + (client.isShiftPressed() ? 1f : 0.1f)) * 10) / 10f, 0.1f, 20f)));

        addDrawableChild(ButtonWidgetHelper.create(width / 2 - 50, height - 62, 100, 20, Text.translatable("speedrunigt.font.apply_and_save"), button -> {
            File config = SpeedRunIGT.FONT_PATH.resolve(fontIdentifier.getFile().getName().substring(0, fontIdentifier.getFile().getName().length() - 4) + ".json").toFile();
            try {
                FileUtils.writeStringToFile(config, newFontConfigure.toString(), StandardCharsets.UTF_8);
                client.reloadResources();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        addDrawableChild(ButtonWidgetHelper.create(width / 2 - 50, height - 40, 100, 20, ScreenTexts.CANCEL, button -> this.close()));
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("IGT: 01:23.456").setStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(fontIdentifier.getIdentifier()))), width / 2, 30, Colors.WHITE);

        context.drawCenteredTextWithShadow(textRenderer, "§l" + I18n.translate("speedrunigt.font.size") + ": " + ((int) newFontConfigure.size), width / 2, height / 2 - 55, Colors.WHITE);
        context.drawCenteredTextWithShadow(textRenderer, "§l" + I18n.translate("speedrunigt.font.oversample") + ": " + newFontConfigure.oversample, width / 2, height / 2 - 5, Colors.WHITE);
        context.drawCenteredTextWithShadow(textRenderer, I18n.translate("speedrunigt.font.oversample.description"), width / 2, height / 2 + 27, Colors.WHITE);
    }
}
