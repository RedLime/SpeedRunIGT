package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.therun.TheRunKeyHelper;
import com.redlimerl.speedrunigt.therun.TheRunRequestHelper;
import com.redlimerl.speedrunigt.utils.OperatingUtils;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.TranslatableText;
import org.lwjgl.input.Keyboard;

public class TheRunUploadKeyScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget uploadKeyBox;
    private ButtonWidget saveButton;
    private Thread keyCheckThread = null;
    private int statusCode = 0;

    public TheRunUploadKeyScreen(Screen parent) {
        super();
        this.parent = parent;
    }

    @Override
    public void method_21947() {
        super.method_21947();
        Keyboard.enableRepeatEvents(true);

        this.saveButton = new ConsumerButtonWidget(field_22535 / 2 - 100, field_22536 / 2 + 24, 98, 20, new TranslatableText("speedrunigt.option.save").asFormattedString(),
                (button) -> {
                    this.saveButton.field_22511 = false;
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
                        this.saveButton.field_22511 = true;
                    });
                    statusCode = 1;
                    this.keyCheckThread.start();
                });
        field_22537.add(this.saveButton);

        field_22537.add(new ConsumerButtonWidget(field_22535 / 2 + 2, field_22536 / 2 + 24, 98, 20, ScreenTexts.CANCEL, (button) -> onClose()));

        field_22537.add(new ConsumerButtonWidget(field_22535 / 2 - 100, field_22536 / 2 + 2, 200, 20, new TranslatableText("speedrunigt.therun_gg.get_upload_key").asFormattedString(), (button) -> OperatingUtils.setUrl("https://therun.gg/upload-key")));


        this.uploadKeyBox = new TextFieldWidget(this.field_22540, this.field_22535 / 2 - 110, field_22536 / 2 - 32, 220, 20);
        this.uploadKeyBox.setMaxLength(36);
        this.uploadKeyBox.setText(TheRunKeyHelper.UPLOAD_KEY);
        this.uploadKeyBox.setFocused(true);
    }

    @Override
    protected void method_21930(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.method_21930(button);
    }

    public void onClose() {
        Keyboard.enableRepeatEvents(false);
        if (this.keyCheckThread != null) this.keyCheckThread.interrupt();
        MinecraftClient.getInstance().openScreen(parent);
    }

    @Override
    public void method_21925(int mouseX, int mouseY, float delta) {
        this.method_21946();
        String beforeText = this.uploadKeyBox.getText();
        this.uploadKeyBox.setText(beforeText.replaceAll("\\w", "*"));

        this.method_21881(this.field_22540, new TranslatableText("speedrunigt.option.therun_gg.edit_upload_key").asFormattedString(), field_22535 / 2, 12, 16777215);

        this.uploadKeyBox.render();

        super.method_21925(mouseX, mouseY, delta);

        if (statusCode == 1)
            this.method_21881(this.field_22540, new TranslatableText("speedrunigt.therun_gg.message.loading_upload_key_info").asFormattedString(), field_22535 / 2, field_22536 / 2 + 50, 16777215);

        if (statusCode == 2)
            this.method_21881(this.field_22540, new TranslatableText("speedrunigt.therun_gg.message.upload_key_is_valid").asFormattedString(), field_22535 / 2, field_22536 / 2 + 50, 16777215);

        if (statusCode == 3)
            this.method_21881(this.field_22540, new TranslatableText("speedrunigt.therun_gg.message.upload_key_is_invalid").asFormattedString(), field_22535 / 2, field_22536 / 2 + 50, 16777215);

        this.uploadKeyBox.setText(beforeText);
    }

    @Override
    public void method_21936() {
        this.uploadKeyBox.tick();
    }

    @Override
    protected void method_21924(char chr, int keyCode) {
        this.uploadKeyBox.keyPressed(chr, keyCode);
    }

}
