package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.therun.TheRunKeyHelper;
import com.redlimerl.speedrunigt.therun.TheRunRequestHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

public class TheRunUploadKeyScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget uploadKeyBox;
    private ButtonWidget saveButton;
    private Thread keyCheckThread;
    private int statusCode = 0;

    public TheRunUploadKeyScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.option.therun_gg.edit_upload_key"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (this.client == null) { return; }
        
        this.client.keyboard.enableRepeatEvents(true);

        this.saveButton = this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 2 + 24, 98, 20, new TranslatableText("selectWorld.edit.save"),
                (button) -> {
                    this.saveButton.active = false;
                    String key = this.uploadKeyBox.getText();
                    this.keyCheckThread = new Thread(() -> {
                        if (TheRunRequestHelper.checkValidUploadKey(key)) {
                            TheRunKeyHelper.UPLOAD_KEY = key;
                            TheRunKeyHelper.save();
                            this.statusCode = 2;
                        } else {
                            this.statusCode = 3;
                        }
                        this.keyCheckThread = null;
                        this.saveButton.active = true;
                    });
                    this.statusCode = 1;
                    this.keyCheckThread.start();
                }));

        this.addButton(new ButtonWidget(this.width / 2 + 2, this.height / 2 + 24, 98, 20, ScreenTexts.CANCEL, (button) -> onClose()));
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 2 + 2, 200, 20, new TranslatableText("speedrunigt.therun_gg.get_upload_key"), (button) -> Util.getOperatingSystem().open("https://therun.gg/upload-key")));
        
        this.uploadKeyBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 110, this.height / 2 - 32, 220, 20, new TranslatableText("speedrunigt.therun_gg.insert_upload_key"));
        this.uploadKeyBox.setMaxLength(36);
        this.uploadKeyBox.setText(TheRunKeyHelper.UPLOAD_KEY);
        
        this.addChild(this.uploadKeyBox);
        this.setInitialFocus(this.uploadKeyBox);
    }

    @Override
    public void onClose() {
        if (this.keyCheckThread != null) this.keyCheckThread.interrupt();
        if (this.client != null) this.client.openScreen(this.parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        
        String beforeText = this.uploadKeyBox.getText();
        this.uploadKeyBox.setText(beforeText.replaceAll("\\w", "*"));

        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 12, 16777215);
        this.uploadKeyBox.render(matrices, mouseX, mouseY, delta);

        if (this.statusCode == 1) {
            this.drawCenteredText(matrices, this.textRenderer, new TranslatableText("speedrunigt.therun_gg.message.loading_upload_key_info"), this.width / 2, this.height / 2 + 50, 16777215);
        } else if (this.statusCode == 2) {
            this.drawCenteredText(matrices, this.textRenderer, new TranslatableText("speedrunigt.therun_gg.message.upload_key_is_valid"), this.width / 2, this.height / 2 + 50, 16777215);
        } else if (this.statusCode == 3) {
            this.drawCenteredText(matrices, this.textRenderer, new TranslatableText("speedrunigt.therun_gg.message.upload_key_is_invalid"), this.width / 2, this.height / 2 + 50, 16777215);
        }

        this.uploadKeyBox.setText(beforeText);
        
        super.render(matrices, mouseX, mouseY, delta);
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
        if (this.client != null) this.client.keyboard.enableRepeatEvents(false);
    }
}
