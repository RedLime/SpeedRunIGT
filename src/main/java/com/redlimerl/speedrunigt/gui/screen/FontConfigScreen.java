package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import com.redlimerl.speedrunigt.utils.FontConfigure;
import com.redlimerl.speedrunigt.utils.FontIdentifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FontConfigScreen extends Screen {
    private final Screen parent;
    private final FontConfigure newFontConfigure;
    private final FontIdentifier fontIdentifier;

    protected FontConfigScreen(Screen parent, Identifier font) {
        super(Component.literal("font_config"));
        this.parent = parent;
        this.fontIdentifier = SpeedRunIGT.FONT_MAPS.get(font);
        this.newFontConfigure = FontConfigure.fromJson(fontIdentifier.getFontConfigure().toString());
    }

    @Override
    protected void init() {
        assert minecraft != null;

        addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 21, height / 2 - 45, 20, 20, Component.literal("-"), button -> newFontConfigure.size = Mth.clamp(newFontConfigure.size - 1, 1, 50)));
        addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 1, height / 2 - 45, 20, 20, Component.literal("+"), button -> newFontConfigure.size = Mth.clamp(newFontConfigure.size + 1, 1, 50)));

        addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 21, height / 2 + 5, 20, 20, Component.literal("-"), button -> newFontConfigure.oversample = Mth.clamp(Math.round((newFontConfigure.oversample - (minecraft.hasShiftDown() ? 1f : 0.1f)) * 10) / 10f, 0.1f, 20f)));
        addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 1, height / 2 + 5, 20, 20, Component.literal("+"), button -> newFontConfigure.oversample = Mth.clamp(Math.round((newFontConfigure.oversample + (minecraft.hasShiftDown() ? 1f : 0.1f)) * 10) / 10f, 0.1f, 20f)));

        addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 50, height - 62, 100, 20, Component.translatable("speedrunigt.font.apply_and_save"), button -> {
            File config = SpeedRunIGT.FONT_PATH.resolve(fontIdentifier.getFile().getName().substring(0, fontIdentifier.getFile().getName().length() - 4) + ".json").toFile();
            try {
                FileUtils.writeStringToFile(config, newFontConfigure.toString(), StandardCharsets.UTF_8);
                minecraft.reloadResourcePacks();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 50, height - 40, 100, 20, CommonComponents.GUI_CANCEL, button -> this.onClose()));
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(font, Component.literal("IGT: 01:23.456").setStyle(Style.EMPTY.withFont(new FontDescription.Resource(fontIdentifier.getIdentifier()))), width / 2, 30, CommonColors.WHITE);

        context.drawCenteredString(font, "§l" + I18n.get("speedrunigt.font.size") + ": " + ((int) newFontConfigure.size), width / 2, height / 2 - 55, CommonColors.WHITE);
        context.drawCenteredString(font, "§l" + I18n.get("speedrunigt.font.oversample") + ": " + newFontConfigure.oversample, width / 2, height / 2 - 5, CommonColors.WHITE);
        context.drawCenteredString(font, I18n.get("speedrunigt.font.oversample.description"), width / 2, height / 2 + 27, CommonColors.WHITE);
    }
}
