package com.redlimerl.speedrunigt.option;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class TimerCustomizeScreen extends Screen {

    private final TimerDrawer drawer = new TimerDrawer(new InGameTimer(), false);
    private final Screen parent;

    private boolean changed = false;
    private ButtonWidget saveButton;

    public TimerCustomizeScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.option.timer_position"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addButton(new SliderWidget(width / 2 - 60, height / 2 - 48, 120, 20, new TranslatableText("speedrunigt.option.timer_position.scale").append(" : ").append(((int) (drawer.getScale() * 100)) + "%"), (drawer.getScale() - 0.5f) / 2.5f) {
            @Override
            protected void updateMessage() {
                this.setMessage(new TranslatableText("speedrunigt.option.timer_position.scale").append(" : ").append(((int) (drawer.getScale() * 100)) + "%"));
            }

            @Override
            protected void applyValue() {
                drawer.setStatus(drawer.getXPos(), drawer.getYPos(), Math.round((float) (0.5f + (this.value * 2.5f)) * 20f)/20f, drawer.getBgOpacity(), drawer.isReversed(), drawer.isSimply());
                changed = true;
            }
        });

        addButton(new SliderWidget(width / 2 - 60, height / 2 - 26, 120, 20, new TranslatableText("speedrunigt.option.timer_position.background_opacity").append(" : ").append(((int) (drawer.getBgOpacity() * 100)) + "%"), drawer.getBgOpacity()) {
            @Override
            protected void updateMessage() {
                this.setMessage(new TranslatableText("speedrunigt.option.timer_position.background_opacity").append(" : ").append(((int) (drawer.getBgOpacity() * 100)) + "%"));
            }

            @Override
            protected void applyValue() {
                drawer.setStatus(drawer.getXPos(), drawer.getYPos(), drawer.getScale(), (float) this.value, drawer.isReversed(), drawer.isSimply());
                changed = true;
            }
        });

        addButton(new ButtonWidget(width / 2 - 60, height / 2 - 4, 120, 20, new TranslatableText("speedrunigt.option.timer_position.top_timer").append(" : ").append(drawer.isReversed() ?  "RTA" : "IGT"), (ButtonWidget button) -> {
            drawer.setStatus(drawer.getXPos(), drawer.getYPos(), drawer.getScale(), drawer.getBgOpacity(), !drawer.isReversed(), drawer.isSimply());
            changed = true;
            button.setMessage(new TranslatableText("speedrunigt.option.timer_position.top_timer").append(" : ").append(drawer.isReversed() ?  "RTA" : "IGT"));
        }));

        addButton(new ButtonWidget(width / 2 - 60, height / 2 + 18, 120, 20, new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimply() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
            drawer.setStatus(drawer.getXPos(), drawer.getYPos(), drawer.getScale(), drawer.getBgOpacity(), drawer.isReversed(), !drawer.isSimply());
            changed = true;
            button.setMessage(new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimply() ? ScreenTexts.ON : ScreenTexts.OFF));
        }));

        this.saveButton = addButton(new ButtonWidget(width / 2 - 60, height / 2 + 40, 58, 20, new TranslatableText("selectWorld.edit.save"), (ButtonWidget button) -> {
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_POSITION_X, drawer.getXPos());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_POSITION_Y, drawer.getYPos());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_SCALE, drawer.getScale());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_BG_OPACITY, drawer.getBgOpacity());
            SpeedRunOptions.setOption(SpeedRunOptions.REVERSED_IGT_RTA, drawer.isReversed());
            SpeedRunOptions.setOption(SpeedRunOptions.DISPLAY_TIME_ONLY, drawer.isSimply());
            changed = false;
        }));

        addButton(new ButtonWidget(width / 2 + 1, height / 2 + 40, 58, 20, ScreenTexts.CANCEL, (ButtonWidget button) -> {
            if (client != null) client.openScreen(parent);
        }));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean isClicked = super.mouseClicked(mouseX, mouseY, button);
        if (!isClicked) {
            drawer.setStatus((float) (mouseX / width), (float) (mouseY / height), drawer.getScale(), drawer.getBgOpacity(), drawer.isReversed(), drawer.isSimply());
            changed = true;
        }
        return isClicked;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        saveButton.active = changed;

        this.renderBackground(matrices);

        drawer.draw();

        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
        this.drawCenteredText(matrices, this.textRenderer,
                new TranslatableText("speedrunigt.option.timer_position.description"), this.width / 2, this.height / 2 - 62, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        assert client != null;
        client.openScreen(parent);
    }
}
