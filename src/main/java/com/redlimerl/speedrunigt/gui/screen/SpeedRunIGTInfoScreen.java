package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.utils.OperatingUtils;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
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
    public void method_21947() {
        checkUpdate();

        update = new ConsumerButtonWidget(field_22535 / 2 - 155, field_22536 - 104, 150, 20, new TranslatableText("speedrunigt.menu.download_update").asFormattedString(), (button) -> OperatingUtils.setUrl(UPDATE_URL));
        update.field_22511 = false;
        field_22537.add(update);
        field_22537.add(new ConsumerButtonWidget(field_22535 / 2 + 5, field_22536 - 104, 150, 20, new TranslatableText("speedrunigt.menu.latest_change_log").asFormattedString(), (button) -> OperatingUtils.setUrl("https://github.com/RedLime/SpeedRunIGT/releases/latest")));

        field_22537.add(new ConsumerButtonWidget(field_22535 / 2 - 155, field_22536 - 80, 150, 20, new TranslatableText("speedrunigt.menu.open_github_repo").asFormattedString(), (button) -> OperatingUtils.setUrl("https://github.com/RedLime/SpeedRunIGT/")));
        field_22537.add(new ConsumerButtonWidget(field_22535 / 2 + 5, field_22536 - 80, 150, 20, new TranslatableText("speedrunigt.menu.open_support_page").asFormattedString(), (button) -> OperatingUtils.setUrl("https://ko-fi.com/redlimerl")));
        field_22537.add(new ConsumerButtonWidget(field_22535 / 2 - 100, field_22536 - 40, 200, 20, ScreenTexts.BACK, (button) -> onClose()));
    }

    @Override
    public void method_21925(int mouseX, int mouseY, float delta) {
        this.method_21946();
        GL11.glPushMatrix();
        GL11.glScalef(1.5f, 1.5f, 1.5f);
        this.method_21881(this.field_22540, new TranslatableText("speedrunigt.title").asFormattedString(), this.field_22535 / 3, 15, 16777215);
        GL11.glPopMatrix();
        this.method_21881(this.field_22540, new LiteralText("Made by RedLime").asFormattedString(), this.field_22535 / 2, 50, 16777215);
        this.method_21881(this.field_22540, new LiteralText("Discord : RedLime#0817").asFormattedString(), this.field_22535 / 2, 62, 16777215);
        this.method_21881(this.field_22540,
                new LiteralText("Version : "+ SpeedRunIGT.MOD_VERSION.split("\\+")[0]).asFormattedString(), this.field_22535 / 2, 78, 16777215);
        if (UPDATE_STATUS != UpdateStatus.NONE) {
            if (UPDATE_STATUS == UpdateStatus.OUTDATED) {
                update.field_22511 = true;
                this.method_21881(this.field_22540, new LiteralText("Updated Version : "+ UPDATE_VERSION).setStyle(new Style().setFormatting(Formatting.YELLOW)).asFormattedString(), this.field_22535 / 2, 88, 16777215);
            }
            this.method_21881(this.field_22540,
                    new TranslatableText("speedrunigt.message.update."+UPDATE_STATUS.name().toLowerCase(Locale.ROOT)).asFormattedString(),
                    this.field_22535 / 2, 116, 16777215);
        }

        super.method_21925(mouseX, mouseY, delta);
    }

    public void onClose() {
        if (this.field_22534 != null) {
            this.field_22534.openScreen(parent);
        }
    }

    @Override
    protected void method_21930(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.method_21930(button);
    }
}
