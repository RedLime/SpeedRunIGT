package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker.UpdateStatus;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.utils.OperatingUtils;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.class_1015;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;

import java.util.Locale;

import static com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker.*;

public class SpeedRunIGTInfoScreen extends Screen {

    private final Screen parent;

    private ClickableWidget update;

    public SpeedRunIGTInfoScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    public void method_2224() {
        checkUpdate();

        update = new ConsumerButtonWidget(field_2561 / 2 - 155, field_2559 - 104, 150, 20, new TranslatableTextContent("speedrunigt.menu.download_update").method_10865(), (button) -> OperatingUtils.setUrl(UPDATE_URL));
        update.field_2078 = false;
        field_2564.add(update);
        field_2564.add(new ConsumerButtonWidget(field_2561 / 2 + 5, field_2559 - 104, 150, 20, new TranslatableTextContent("speedrunigt.menu.latest_change_log").method_10865(), (button) -> OperatingUtils.setUrl("https://github.com/RedLime/SpeedRunIGT/releases/latest")));

        field_2564.add(new ConsumerButtonWidget(field_2561 / 2 - 155, field_2559 - 80, 150, 20, new TranslatableTextContent("speedrunigt.menu.open_github_repo").method_10865(), (button) -> OperatingUtils.setUrl("https://github.com/RedLime/SpeedRunIGT/")));
        field_2564.add(new ConsumerButtonWidget(field_2561 / 2 + 5, field_2559 - 80, 150, 20, new TranslatableTextContent("speedrunigt.menu.open_support_page").method_10865(), (button) -> OperatingUtils.setUrl("https://ko-fi.com/redlimerl")));
        field_2564.add(new ConsumerButtonWidget(field_2561 / 2 - 100, field_2559 - 40, 200, 20, ScreenTexts.BACK, (button) -> onClose()));
    }

    @Override
    public void method_2214(int mouseX, int mouseY, float delta) {
        this.method_2240();
        class_1015.method_4461();
        class_1015.method_4384(1.5f, 1.5f, 1.5f);
        this.method_1789(this.field_2554, new TranslatableTextContent("speedrunigt.title").method_10865(), this.field_2561 / 3, 15, 16777215);
        class_1015.method_4350();
        this.method_1789(this.field_2554, new LiteralTextContent("Made by RedLime").method_10865(), this.field_2561 / 2, 50, 16777215);
        this.method_1789(this.field_2554, new LiteralTextContent("Discord : RedLime#0817").method_10865(), this.field_2561 / 2, 62, 16777215);
        this.method_1789(this.field_2554,
                new LiteralTextContent("Version : "+ SpeedRunIGT.MOD_VERSION.split("\\+")[0]).method_10865(), this.field_2561 / 2, 78, 16777215);
        if (UPDATE_STATUS != UpdateStatus.NONE) {
            if (UPDATE_STATUS == UpdateStatus.OUTDATED) {
                update.field_2078 = true;
                this.method_1789(this.field_2554, new LiteralTextContent("Updated Version : "+ UPDATE_VERSION).setStyle(new Style().withColor(Formatting.YELLOW)).method_10865(), this.field_2561 / 2, 88, 16777215);
            }
            this.method_1789(this.field_2554,
                    new TranslatableTextContent("speedrunigt.message.update."+UPDATE_STATUS.name().toLowerCase(Locale.ROOT)).method_10865(),
                    this.field_2561 / 2, 116, 16777215);
        }

        super.method_2214(mouseX, mouseY, delta);
    }

    public void onClose() {
        if (this.field_2563 != null) {
            this.field_2563.setScreen(parent);
        }
    }

    @Override
    protected void method_0_2778(ClickableWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.method_0_2778(button);
    }
}
