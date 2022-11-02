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
    public void init() {
        super.init();
        assert client != null;
        Keyboard.enableRepeatEvents(true);

        this.saveButton = new ConsumerButtonWidget(width / 2 - 100, height / 2 + 24, 98, 20, new TranslatableText("selectWorld.edit.save").asFormattedString(),
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
                });
        method_13411(this.saveButton);

        method_13411(new ConsumerButtonWidget(width / 2 + 2, height / 2 + 24, 98, 20, ScreenTexts.CANCEL, (button) -> onClose()));

        method_13411(new ConsumerButtonWidget(width / 2 - 100, height / 2 + 2, 200, 20, new TranslatableText("speedrunigt.therun_gg.get_upload_key").asFormattedString(), (button) -> OperatingUtils.setUrl("https://therun.gg/upload-key")));


        this.uploadKeyBox = new TextFieldWidget(0, this.textRenderer, this.width / 2 - 110, height / 2 - 32, 220, 20);
        this.uploadKeyBox.setMaxLength(36);
        this.uploadKeyBox.setText(TheRunKeyHelper.UPLOAD_KEY);
        this.uploadKeyBox.setFocused(true);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.buttonClicked(button);
    }

    public void onClose() {
        if (client != null) Keyboard.enableRepeatEvents(false);
        if (this.keyCheckThread != null) this.keyCheckThread.interrupt();
        MinecraftClient.getInstance().openScreen(parent);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        String beforeText = this.uploadKeyBox.getText();
        this.uploadKeyBox.setText(beforeText.replaceAll("\\w", "*"));

        this.drawCenteredString(this.textRenderer, new TranslatableText("speedrunigt.option.therun_gg.edit_upload_key").asFormattedString(), width / 2, 12, 16777215);

        this.uploadKeyBox.render();

        super.render(mouseX, mouseY, delta);

        if (statusCode == 1)
            this.drawCenteredString(this.textRenderer, new TranslatableText("speedrunigt.therun_gg.message.loading_upload_key_info").asFormattedString(), width / 2, height / 2 + 50, 16777215);

        if (statusCode == 2)
            this.drawCenteredString(this.textRenderer, new TranslatableText("speedrunigt.therun_gg.message.upload_key_is_valid").asFormattedString(), width / 2, height / 2 + 50, 16777215);

        if (statusCode == 3)
            this.drawCenteredString(this.textRenderer, new TranslatableText("speedrunigt.therun_gg.message.upload_key_is_invalid").asFormattedString(), width / 2, height / 2 + 50, 16777215);

        this.uploadKeyBox.setText(beforeText);
    }

    @Override
    public void tick() {
        this.uploadKeyBox.tick();
    }

    @Override
    protected void keyPressed(char chr, int keyCode) {
        this.uploadKeyBox.keyPressed(chr, keyCode);
    }

    @Override
    public void removed() {
        if (client != null) Keyboard.enableRepeatEvents(false);
    }
}
