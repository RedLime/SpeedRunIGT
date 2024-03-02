package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.therun.TheRunKeyHelper;
import com.redlimerl.speedrunigt.therun.TheRunRequestHelper;
import com.redlimerl.speedrunigt.utils.OperatingUtils;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.TranslatableTextContent;
import org.lwjgl.input.Keyboard;

public class TheRunUploadKeyScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget uploadKeyBox;
    private ClickableWidget saveButton;
    private Thread keyCheckThread = null;
    private int statusCode = 0;

    public TheRunUploadKeyScreen(Screen parent) {
        super();
        this.parent = parent;
    }

    @Override
    public void method_2224() {
        super.method_2224();
        assert field_2563 != null;
        Keyboard.enableRepeatEvents(true);

        this.saveButton = new ConsumerButtonWidget(field_2561 / 2 - 100, field_2559 / 2 + 24, 98, 20, new TranslatableTextContent("selectWorld.edit.save").method_10865(),
                (button) -> {
                    this.saveButton.field_2078 = false;
                    String key = uploadKeyBox.getText();
                    this.keyCheckThread = new Thread(() -> {
                        if (TheRunRequestHelper.checkValidUploadKey(key)) {
                            TheRunKeyHelper.UPLOAD_KEY = key;
                            TheRunKeyHelper.save();
                            statusCode = 2;
                        } else {
                            statusCode = 3;
                        }
                        this.keyCheckThread = null;
                        this.saveButton.field_2078 = true;
                    });
                    statusCode = 1;
                    this.keyCheckThread.start();
                });
        method_2219(this.saveButton);

        method_2219(new ConsumerButtonWidget(field_2561 / 2 + 2, field_2559 / 2 + 24, 98, 20, ScreenTexts.CANCEL, (button) -> onClose()));

        method_2219(new ConsumerButtonWidget(field_2561 / 2 - 100, field_2559 / 2 + 2, 200, 20, new TranslatableTextContent("speedrunigt.therun_gg.get_upload_key").method_10865(), (button) -> OperatingUtils.setUrl("https://therun.gg/upload-key")));


        this.uploadKeyBox = new TextFieldWidget(0, this.field_2554, this.field_2561 / 2 - 110, field_2559 / 2 - 32, 220, 20);
        this.uploadKeyBox.setMaxLength(36);
        this.uploadKeyBox.setText(TheRunKeyHelper.UPLOAD_KEY);
        this.uploadKeyBox.setTextFieldFocused(true);
    }

    @Override
    protected void method_0_2778(ClickableWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.method_0_2778(button);
    }

    public void onClose() {
        if (field_2563 != null) Keyboard.enableRepeatEvents(false);
        if (this.keyCheckThread != null) this.keyCheckThread.interrupt();
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void method_2214(int mouseX, int mouseY, float delta) {
        this.method_2240();
        String beforeText = this.uploadKeyBox.getText();
        this.uploadKeyBox.setText(beforeText.replaceAll("\\w", "*"));

        this.method_1789(this.field_2554, new TranslatableTextContent("speedrunigt.option.therun_gg.edit_upload_key").method_10865(), field_2561 / 2, 12, 16777215);

        this.uploadKeyBox.method_1857();

        super.method_2214(mouseX, mouseY, delta);

        if (statusCode == 1)
            this.method_1789(this.field_2554, new TranslatableTextContent("speedrunigt.therun_gg.message.loading_upload_key_info").method_10865(), field_2561 / 2, field_2559 / 2 + 50, 16777215);

        if (statusCode == 2)
            this.method_1789(this.field_2554, new TranslatableTextContent("speedrunigt.therun_gg.message.upload_key_is_valid").method_10865(), field_2561 / 2, field_2559 / 2 + 50, 16777215);

        if (statusCode == 3)
            this.method_1789(this.field_2554, new TranslatableTextContent("speedrunigt.therun_gg.message.upload_key_is_invalid").method_10865(), field_2561 / 2, field_2559 / 2 + 50, 16777215);

        this.uploadKeyBox.setText(beforeText);
    }

    @Override
    public void method_2225() {
        this.uploadKeyBox.tick();
    }

    @Override
    protected void method_0_2773(char chr, int keyCode) {
        this.uploadKeyBox.method_0_2506(chr, keyCode);
    }

    @Override
    public void method_2234() {
        if (field_2563 != null) Keyboard.enableRepeatEvents(false);
    }
}
