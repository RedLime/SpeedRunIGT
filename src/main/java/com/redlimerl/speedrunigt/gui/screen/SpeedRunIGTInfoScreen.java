package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker.UpdateStatus;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.utils.OperatingUtils;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import org.lwjgl.opengl.GL11;

import java.util.Locale;

import static com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker.*;

public class SpeedRunIGTInfoScreen extends Screen {

    private final Screen parent;

    private ButtonWidget update;

    public SpeedRunIGTInfoScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    public void init() {
        checkUpdate();

        update = new ConsumerButtonWidget(width / 2 - 155, height - 104, 150, 20, I18n.translate("speedrunigt.menu.download_update"), (button) -> OperatingUtils.setUrl(UPDATE_URL));
        update.active = false;
        buttons.add(update);
        buttons.add(new ConsumerButtonWidget(width / 2 + 5, height - 104, 150, 20, I18n.translate("speedrunigt.menu.latest_change_log"), (button) -> OperatingUtils.setUrl("https://github.com/RedLime/SpeedRunIGT/releases/latest")));

        buttons.add(new ConsumerButtonWidget(width / 2 - 155, height - 80, 150, 20, I18n.translate("speedrunigt.menu.open_github_repo"), (button) -> OperatingUtils.setUrl("https://github.com/RedLime/SpeedRunIGT/")));
        buttons.add(new ConsumerButtonWidget(width / 2 + 5, height - 80, 150, 20, I18n.translate("speedrunigt.menu.open_support_page"), (button) -> OperatingUtils.setUrl("https://ko-fi.com/redlimerl")));
        buttons.add(new ConsumerButtonWidget(width / 2 - 100, height - 40, 200, 20, ScreenTexts.BACK, (button) -> onClose()));
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        GL11.glPushMatrix();
        GL11.glScalef(1.5f, 1.5f, 1.5f);
        this.drawCenteredString(this.textRenderer, I18n.translate("speedrunigt.title"), this.width / 3, 15, 16777215);
        GL11.glPopMatrix();
        this.drawCenteredString(this.textRenderer, "Made by RedLime", this.width / 2, 50, 16777215);
        this.drawCenteredString(this.textRenderer, "Discord : RedLime#0817", this.width / 2, 62, 16777215);
        this.drawCenteredString(this.textRenderer,
                "Version : "+ SpeedRunIGT.MOD_VERSION.split("\\+")[0], this.width / 2, 78, 16777215);
        if (UPDATE_STATUS != UpdateStatus.NONE) {
            if (UPDATE_STATUS == UpdateStatus.OUTDATED) {
                update.active = true;
                this.drawCenteredString(this.textRenderer, "Updated Version : "+ UPDATE_VERSION, this.width / 2, 88, 16777215);
            }
            this.drawCenteredString(this.textRenderer,
                    I18n.translate("speedrunigt.message.update."+UPDATE_STATUS.name().toLowerCase(Locale.ROOT)),
                    this.width / 2, 116, 16777215);
        }

        super.render(mouseX, mouseY, delta);
    }

    public void onClose() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.buttonClicked(button);
    }
}
