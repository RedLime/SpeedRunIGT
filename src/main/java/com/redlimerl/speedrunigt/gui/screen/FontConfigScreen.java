package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.mixins.access.FontManagerAccessor;
import com.redlimerl.speedrunigt.mixins.access.MinecraftClientAccessor;
import com.redlimerl.speedrunigt.utils.FontConfigure;
import com.redlimerl.speedrunigt.utils.FontIdentifier;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
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
        assert minecraft != null;

        addButton(new ButtonWidget(width / 2 - 21, height / 2 - 45, 20, 20, "-", button -> newFontConfigure.size = MathHelper.clamp(newFontConfigure.size - 1, 1, 50)));
        addButton(new ButtonWidget(width / 2 + 1, height / 2 - 45, 20, 20, "+", button -> newFontConfigure.size = MathHelper.clamp(newFontConfigure.size + 1, 1, 50)));

        addButton(new ButtonWidget(width / 2 - 21, height / 2 + 5, 20, 20, "-", button -> newFontConfigure.oversample = MathHelper.clamp(Math.round((newFontConfigure.oversample - (Screen.hasShiftDown() ? 1f : 0.1f)) * 10) / 10f, 0.1f, 20f)));
        addButton(new ButtonWidget(width / 2 + 1, height / 2 + 5, 20, 20, "+", button -> newFontConfigure.oversample = MathHelper.clamp(Math.round((newFontConfigure.oversample + (Screen.hasShiftDown() ? 1f : 0.1f)) * 10) / 10f, 0.1f, 20f)));

        addButton(new ButtonWidget(width / 2 - 50, height - 62, 100, 20, I18n.translate("speedrunigt.font.apply_and_save"), button -> {
            File config = SpeedRunIGT.FONT_PATH.resolve(fontIdentifier.getFile().getName().substring(0, fontIdentifier.getFile().getName().length() - 4) + ".json").toFile();
            try {
                FileUtils.writeStringToFile(config, newFontConfigure.toString(), StandardCharsets.UTF_8);
                minecraft.reloadResources();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        addButton(new ButtonWidget(width / 2 - 50, height - 40, 100, 20, ScreenTexts.CANCEL, button -> this.onClose()));
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.openScreen(parent);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        renderBackground();

        FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftClientAccessor) MinecraftClient.getInstance()).getFontManager();
        TextRenderer targetFont = fontManager.getTextRenderers().get(fontIdentifier.getIdentifier());

        drawCenteredString(targetFont, "IGT: 01:23.456", width / 2, 30, 16777215);

        drawCenteredString(font, "§l" + I18n.translate("speedrunigt.font.size") + ": " + ((int) newFontConfigure.size), width / 2, height / 2 - 55, 16777215);
        drawCenteredString(font, "§l" + I18n.translate("speedrunigt.font.oversample") + ": " + newFontConfigure.oversample, width / 2, height / 2 - 5, 16777215);
        drawCenteredString(font, I18n.translate("speedrunigt.font.oversample.description"), width / 2, height / 2 + 27, 16777215);

        super.render(mouseX, mouseY, delta);
    }
}
