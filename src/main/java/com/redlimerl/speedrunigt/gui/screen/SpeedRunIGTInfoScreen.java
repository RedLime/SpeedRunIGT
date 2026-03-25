package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Util;

import java.util.Locale;

import static com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker.*;

public class SpeedRunIGTInfoScreen extends Screen {

    private final Screen parent;

    private Button update;

    public SpeedRunIGTInfoScreen(Screen parent) {
        super(Component.translatable("speedrunigt.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        checkUpdate();
        assert minecraft != null;
        update = addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 155, height - 104, 150, 20, Component.translatable("speedrunigt.menu.download_update"), (Button button) -> Util.getPlatform().openUri(UPDATE_URL)));
        update.active = false;
        addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 5, height - 104, 150, 20, Component.translatable("speedrunigt.menu.latest_change_log"), (Button button) -> Util.getPlatform().openUri("https://github.com/RedLime/SpeedRunIGT/releases/latest")));

        addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 155, height - 80, 150, 20, Component.translatable("speedrunigt.menu.open_github_repo"), (Button button) -> Util.getPlatform().openUri("https://github.com/RedLime/SpeedRunIGT/")));
        addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 5, height - 80, 150, 20, Component.translatable("speedrunigt.menu.open_support_page"), (Button button) -> Util.getPlatform().openUri("https://ko-fi.com/redlimerl")));
        addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 100, height - 40, 200, 20, CommonComponents.GUI_BACK, (Button button) -> minecraft.setScreen(parent)));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.pose().pushMatrix();
        context.pose().scale(1.5F, 1.5F);
        context.drawCenteredString(this.font, this.title, this.width / 3, 15, CommonColors.WHITE);
        context.pose().popMatrix();

        context.drawCenteredString(this.font,
                Component.literal("Version : "+ SpeedRunIGT.MOD_VERSION.split("\\+")[0]), this.width / 2, 78, CommonColors.WHITE);
        if (UPDATE_STATUS != UpdateStatus.NONE) {
            if (UPDATE_STATUS == UpdateStatus.OUTDATED) {
                update.active = true;
                context.drawCenteredString(this.font, Component.literal("Updated Version : "+ UPDATE_VERSION).withStyle(ChatFormatting.YELLOW), this.width / 2, 88, CommonColors.WHITE);
            }
            context.drawCenteredString(this.font,
                    Component.translatable("speedrunigt.message.update."+UPDATE_STATUS.name().toLowerCase(Locale.ROOT)),
                    this.width / 2, 116, CommonColors.WHITE);
        }
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }
}
