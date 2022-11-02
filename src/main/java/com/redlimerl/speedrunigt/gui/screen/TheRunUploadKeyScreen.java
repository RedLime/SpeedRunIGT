package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.therun.TheRunKeyHelper;
import com.redlimerl.speedrunigt.therun.TheRunRequestHelper;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

public class TheRunUploadKeyScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget uploadKeyBox;
    private ButtonWidget saveButton;
    private Thread keyCheckThread = null;
    private int statusCode = 0;

    public TheRunUploadKeyScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.option.therun_gg.edit_upload_key"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        assert minecraft != null;
        minecraft.keyboard.enableRepeatEvents(true);

        this.saveButton = addButton(new ButtonWidget(width / 2 - 100, height / 2 + 24, 98, 20, new TranslatableText("selectWorld.edit.save").asFormattedString(),
                (button) -> {
                    this.saveButton.active = false;
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
                        this.saveButton.active = true;
                    });
                    statusCode = 1;
                    this.keyCheckThread.start();
                }));

        addButton(new ButtonWidget(width / 2 + 2, height / 2 + 24, 98, 20, ScreenTexts.CANCEL, (button) -> onClose()));

        addButton(new ButtonWidget(width / 2 - 100, height / 2 + 2, 200, 20, new TranslatableText("speedrunigt.therun_gg.get_upload_key").asFormattedString(), (button) -> Util.getOperatingSystem().open("https://therun.gg/upload-key")));


        this.uploadKeyBox = new TextFieldWidget(this.font, this.width / 2 - 110, height / 2 - 32, 220, 20, new TranslatableText("speedrunigt.therun_gg.insert_upload_key").asFormattedString());
        this.uploadKeyBox.setMaxLength(36);
        this.uploadKeyBox.setText(TheRunKeyHelper.UPLOAD_KEY);
        children.add(this.uploadKeyBox);
        setInitialFocus(this.uploadKeyBox);
    }

    @Override
    public void onClose() {
        super.onClose();
        if (this.keyCheckThread != null) this.keyCheckThread.interrupt();
        MinecraftClient.getInstance().openScreen(parent);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        String beforeText = this.uploadKeyBox.getText();
        this.uploadKeyBox.setText(beforeText.replaceAll("\\w", "*"));

        this.drawCenteredString(font, title.asFormattedString(), width / 2, 12, 16777215);

        this.uploadKeyBox.render(mouseX, mouseY, delta);

        super.render(mouseX, mouseY, delta);

        if (statusCode == 1)
            this.drawCenteredString(font, new TranslatableText("speedrunigt.therun_gg.message.loading_upload_key_info").asFormattedString(), width / 2, height / 2 + 50, 16777215);

        if (statusCode == 2)
            this.drawCenteredString(font, new TranslatableText("speedrunigt.therun_gg.message.upload_key_is_valid").asFormattedString(), width / 2, height / 2 + 50, 16777215);

        if (statusCode == 3)
            this.drawCenteredString(font, new TranslatableText("speedrunigt.therun_gg.message.upload_key_is_invalid").asFormattedString(), width / 2, height / 2 + 50, 16777215);

        this.uploadKeyBox.setText(beforeText);
    }

    @Override
    public void tick() {
        this.uploadKeyBox.tick();
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        return this.uploadKeyBox.charTyped(chr, keyCode);
    }

    @Override
    public void removed() {
        if (minecraft != null) minecraft.keyboard.enableRepeatEvents(false);
    }
}
