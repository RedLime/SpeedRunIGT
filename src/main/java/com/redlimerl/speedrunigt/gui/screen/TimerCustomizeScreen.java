package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecimals;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.version.ColorMixer;
import com.redlimerl.speedrunigt.version.CustomSliderWidget;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Locale;

public class TimerCustomizeScreen extends Screen {

    private final TimerDrawer drawer = new TimerDrawer(false);
    private final Screen parent;

    private boolean changed = false;
    private boolean hide = false;
    private final ArrayList<ButtonWidget> normalOptions = new ArrayList<>();
    private final ArrayList<ButtonWidget> igtOptions = new ArrayList<>();
    private final ArrayList<ButtonWidget> rtaOptions = new ArrayList<>();
    private final ArrayList<ButtonWidget> backgroundOptions = new ArrayList<>();
    private ConsumerButtonWidget normalButton;
    private ConsumerButtonWidget igtButton;
    private ConsumerButtonWidget rtaButton;
    private ConsumerButtonWidget backgroundButton;
    private ConsumerButtonWidget saveButton;

    public TimerCustomizeScreen(Screen parent) {
        super();
        this.parent = parent;
    }

    private int currentTab = 0;
    private void openTab(int tab) {
        currentTab = tab;
        this.normalButton.active = tab != 0;
        this.igtButton.active = tab != 1;
        this.rtaButton.active = tab != 2;
        this.backgroundButton.active = tab != 4;

        if (hide) return;
        for (ButtonWidget normalOption : normalOptions) {
            normalOption.visible = tab == 0;
        }
        for (ButtonWidget igtOption : igtOptions) {
            igtOption.visible = tab == 1;
        }
        for (ButtonWidget rtaOption : rtaOptions) {
            rtaOption.visible = tab == 2;
        }
        for (ButtonWidget backgroundOption : backgroundOptions) {
            backgroundOption.visible = tab == 4;
        }
    }

    public <T extends ButtonWidget> T addButton(T button) {
        method_13411(button);
        return button;
    }

    @Override
    public void init() {
        normalOptions.clear();
        igtOptions.clear();
        rtaOptions.clear();
        backgroundOptions.clear();

        initNormal();
        initIGTButtons();
        initRTAButtons();
        initBackgroundButtons();

        this.normalButton = new ConsumerButtonWidget(width / 2 - 149, height / 2 - 48, 58, 20, new TranslatableText("options.title").append("...").asFormattedString(), (button) -> openTab(0));
        method_13411(this.normalButton);

        this.igtButton = new ConsumerButtonWidget(width / 2 - 89, height / 2 - 48, 58, 20, new LiteralText("IGT...").asFormattedString(), (button) -> openTab(1));
        method_13411(this.igtButton);

        this.rtaButton = new ConsumerButtonWidget(width / 2 - 29, height / 2 - 48, 58, 20, new LiteralText("RTA...").asFormattedString(), (button) -> openTab(2));
        method_13411(this.rtaButton);

        ButtonWidget fontButton = new ConsumerButtonWidget(width / 2 + 31, height / 2 - 48, 58, 20, new TranslatableText("speedrunigt.title.font").asFormattedString(), button -> {});
        fontButton.active = false;
        method_13411(fontButton);

        this.backgroundButton = new ConsumerButtonWidget(width / 2 + 91, height / 2 - 48, 58, 20, new TranslatableText("speedrunigt.title.background").asFormattedString(), (button) -> openTab(4));
        method_13411(this.backgroundButton);

        method_13411(new ConsumerButtonWidget(width / 2 - 89, height / 2 + 62, 58, 20, new TranslatableText("speedrunigt.option.hide").asFormattedString(), (button) -> {
            hide = !hide;
            for (ButtonWidget normalOption : normalOptions) {
                normalOption.visible = !hide && currentTab == 0;
            }
            for (ButtonWidget igtOption : igtOptions) {
                igtOption.visible = !hide && currentTab == 1;
            }
            for (ButtonWidget rtaOption : rtaOptions) {
                rtaOption.visible = !hide && currentTab == 2;
            }
            for (ButtonWidget backgroundOption : backgroundOptions) {
                backgroundOption.visible = !hide && currentTab == 4;
            }
            openTab(currentTab);
            button.message = new TranslatableText("speedrunigt.option." + (!hide ? "hide" : "show")).asFormattedString();
        }));

        this.saveButton = new ConsumerButtonWidget(width / 2 - 29, height / 2 + 62, 58, 20, new TranslatableText("speedrunigt.option.save").asFormattedString(), (button) -> {
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_POSITION_X, drawer.getIGT_XPos());
            SpeedRunIGT.TIMER_DRAWER.setIGT_XPos(drawer.getIGT_XPos());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_POSITION_Y, drawer.getIGT_YPos());
            SpeedRunIGT.TIMER_DRAWER.setIGT_YPos(drawer.getIGT_YPos());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_SCALE, drawer.getIGTScale());
            SpeedRunIGT.TIMER_DRAWER.setIGTScale(drawer.getIGTScale());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_COLOR, drawer.getIGTColor());
            SpeedRunIGT.TIMER_DRAWER.setIGTColor(drawer.getIGTColor());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_DECO, drawer.getIGTDecoration());
            SpeedRunIGT.TIMER_DRAWER.setIGTDecoration(drawer.getIGTDecoration());

            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_POSITION_X, drawer.getRTA_XPos());
            SpeedRunIGT.TIMER_DRAWER.setRTA_XPos(drawer.getRTA_XPos());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_POSITION_Y, drawer.getRTA_YPos());
            SpeedRunIGT.TIMER_DRAWER.setRTA_YPos(drawer.getRTA_YPos());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_SCALE, drawer.getRTAScale());
            SpeedRunIGT.TIMER_DRAWER.setRTAScale(drawer.getRTAScale());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_COLOR, drawer.getRTAColor());
            SpeedRunIGT.TIMER_DRAWER.setRTAColor(drawer.getRTAColor());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_DECO, drawer.getRTADecoration());
            SpeedRunIGT.TIMER_DRAWER.setRTADecoration(drawer.getRTADecoration());

            SpeedRunOption.setOption(SpeedRunOptions.DISPLAY_TIME_ONLY, drawer.isSimplyTimer());
            SpeedRunIGT.TIMER_DRAWER.setSimplyTimer(drawer.isSimplyTimer());
            SpeedRunOption.setOption(SpeedRunOptions.LOCK_TIMER_POSITION, drawer.isLocked());
            SpeedRunIGT.TIMER_DRAWER.setLocked(drawer.isLocked());
            SpeedRunOption.setOption(SpeedRunOptions.DISPLAY_DECIMALS, drawer.getTimerDecimals());
            SpeedRunIGT.TIMER_DRAWER.setTimerDecimals(drawer.getTimerDecimals());

            SpeedRunOption.setOption(SpeedRunOptions.BACKGROUND_OPACITY, drawer.getBGOpacity());
            SpeedRunIGT.TIMER_DRAWER.setBGOpacity(drawer.getBGOpacity());
            SpeedRunOption.setOption(SpeedRunOptions.RTA_BACKGROUND_PADDING, drawer.getRTAPadding());
            SpeedRunIGT.TIMER_DRAWER.setRTAPadding(drawer.getRTAPadding());
            SpeedRunOption.setOption(SpeedRunOptions.IGT_BACKGROUND_PADDING, drawer.getIGTPadding());
            SpeedRunIGT.TIMER_DRAWER.setIGTPadding(drawer.getIGTPadding());

            changed = false;
        });
        method_13411(this.saveButton);

        method_13411(new ConsumerButtonWidget(width / 2 + 31, height / 2 + 62, 58, 20, ScreenTexts.CANCEL, (button) -> onClose()));

        openTab(0);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean isButtonClick = super.mouseReleased(mouseX, mouseY, button);
        if (button == 0 && !drawer.isLocked()&&!isButtonClick) {
            if (!this.igtButton.active) {
                drawer.setIGT_XPos((float) MathHelper.clamp(mouseX / client.field_19944.method_18321(), 0, 1));
                drawer.setIGT_YPos((float) MathHelper.clamp(mouseY /  client.field_19944.method_18322(), 0, 1));
                changed = true;
            }
            if (!this.rtaButton.active) {
                drawer.setRTA_XPos((float) MathHelper.clamp(mouseX / client.field_19944.method_18321(), 0, 1));
                drawer.setRTA_YPos((float) MathHelper.clamp(mouseY /  client.field_19944.method_18322(), 0, 1));
                changed = true;
            }
        }
        return isButtonClick;
    }

    @Override
    public boolean keyPressed(int keyCode, int j, int k) {
        if (keyCode >= 262 && keyCode <= 265 && client != null && !drawer.isLocked()) {
            int moveX = keyCode == 262 ? 1 : keyCode == 263 ? -1 : 0;
            int moveY = keyCode == 265 ? -1 : keyCode == 264 ? 1 : 0;
            if (!igtButton.active) {
                drawer.setIGT_XPos(MathHelper.clamp(drawer.getIGT_XPos() + moveX * drawer.getIGTScale() / client.field_19944.method_18319(), 0, 1));
                drawer.setIGT_YPos(MathHelper.clamp(drawer.getIGT_YPos() + moveY * drawer.getIGTScale() / client.field_19944.method_18320(), 0, 1));
                changed = true;
            }
            if (!rtaButton.active) {
                drawer.setRTA_XPos(MathHelper.clamp(drawer.getRTA_XPos() + moveX * drawer.getRTAScale() / client.field_19944.method_18319(), 0, 1));
                drawer.setRTA_YPos(MathHelper.clamp(drawer.getRTA_YPos() + moveY * drawer.getRTAScale() / client.field_19944.method_18320(), 0, 1));
                changed = true;
            }
        }
        return super.keyPressed(keyCode, j, k);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        saveButton.active = changed;

        this.renderBackground();

        drawer.draw();

        drawCenteredString(this.textRenderer, new TranslatableText("speedrunigt.option.timer_position").asFormattedString(), this.width / 2, 15, 16777215);

        if (!hide) {
            if (!igtButton.active || !rtaButton.active) {
                if (drawer.isLocked()) {
                    drawCenteredString(this.textRenderer,
                            new TranslatableText("speedrunigt.option.timer_position.description.lock").asFormattedString(), this.width / 2, this.height / 2 - 80, 16777215);
                } else {
                    drawCenteredString(this.textRenderer,
                            new TranslatableText("speedrunigt.option.timer_position.description").asFormattedString(), this.width / 2, this.height / 2 - 80, 16777215);
                    drawCenteredString(this.textRenderer,
                            new TranslatableText("speedrunigt.option.timer_position.description.move").asFormattedString(), this.width / 2, this.height / 2 - 69, 16777215);
                }
            }
        }
        super.render(mouseX, mouseY, delta);
    }

    public void onClose() {
        assert this.client != null;
        this.client.openScreen(parent);
    }

    public void initNormal() {
        normalOptions.add(
                addButton(new ConsumerButtonWidget(width / 2 - 80, height / 2 - 16, 160, 20, new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(), (button) -> {
                    drawer.setSimplyTimer(!drawer.isSimplyTimer());
                    changed = true;
                    button.message = (new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                }))
        );

        normalOptions.add(
                addButton(new ConsumerButtonWidget(width / 2 - 80, height / 2 + 6, 160, 20, new TranslatableText("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(), (button) -> {
                    drawer.setLocked(!drawer.isLocked());
                    changed = true;
                    button.message = (new TranslatableText("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                }))
        );

        normalOptions.add(
                addButton(new ConsumerButtonWidget(width / 2 - 80, height / 2 + 28, 160, 20, new TranslatableText("speedrunigt.option.timer_position.show_decimals").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.show_decimals.context", drawer.getTimerDecimals().getNumber())).asFormattedString(), (button) -> {
                    int order = drawer.getTimerDecimals().ordinal();
                    drawer.setTimerDecimals(TimerDecimals.values()[(++order) % TimerDecimals.values().length]);
                    changed = true;
                    button.message = (new TranslatableText("speedrunigt.option.timer_position.show_decimals").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.show_decimals.context", drawer.getTimerDecimals().getNumber())).asFormattedString());
                }))
        );
    }

    public void initIGTButtons() {
        igtOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 - 16, 120, 20, ColorMixer.getRed(drawer.getIGTColor()) / 255.0f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableText("speedrunigt.option.timer_position.color_red", "IGT").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getIGTColor()))).asFormattedString());
                    }

                    @Override
                    public void applyValue(float value) {
                        int color = drawer.getIGTColor();
                        drawer.setIGTColor(
                                ColorMixer.getArgb(
                                        ColorMixer.getAlpha(color),
                                        (int) (value * 255),
                                        ColorMixer.getGreen(color),
                                        ColorMixer.getBlue(color)
                                )
                        );
                        changed = true;
                    }
                }))
        );

        igtOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 + 6, 120, 20, ColorMixer.getGreen(drawer.getIGTColor()) / 255.0f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableText("speedrunigt.option.timer_position.color_green", "IGT").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getIGTColor()))).asFormattedString());
                    }

                    @Override
                    public void applyValue(float value) {
                        int color = drawer.getIGTColor();
                        drawer.setIGTColor(
                                ColorMixer.getArgb(
                                        ColorMixer.getAlpha(color),
                                        ColorMixer.getRed(color),
                                        (int) (value * 255),
                                        ColorMixer.getBlue(color)
                                )
                        );
                        changed = true;
                    }
                }))
        );

        igtOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 + 28, 120, 20, ColorMixer.getBlue(drawer.getIGTColor()) / 255.0f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableText("speedrunigt.option.timer_position.color_blue", "IGT").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getIGTColor()))).asFormattedString());
                    }

                    @Override
                    public void applyValue(float value) {
                        int color = drawer.getIGTColor();
                        drawer.setIGTColor(
                                ColorMixer.getArgb(
                                        ColorMixer.getAlpha(color),
                                        ColorMixer.getRed(color),
                                        ColorMixer.getGreen(color),
                                        (int) (value * 255)
                                )
                        );
                        changed = true;
                    }
                }))
        );

        igtOptions.add(
                addButton(new CustomSliderWidget(width / 2 + 6, height / 2 - 16, 120, 20, drawer.getIGTScale() / 3f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableText("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append(((int) (drawer.getIGTScale() * 100)) + "%").asFormattedString());
                    }

                    @Override
                    public void applyValue(float value) {
                        drawer.setIGTScale(Math.round(value * 3f * 20f)/20f);
                        changed = true;
                    }
                }))
        );

        igtOptions.add(
                addButton(new ConsumerButtonWidget(width / 2 + 6, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_decorate", "IGT").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))).asFormattedString(), (button) -> {
                    int order = drawer.getIGTDecoration().ordinal();
                    drawer.setIGTDecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    changed = true;
                    button.message = (new TranslatableText("speedrunigt.option.timer_position.text_decorate", "IGT").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))).asFormattedString());
                }))
        );
    }

    public void initRTAButtons() {
        rtaOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 - 16, 120, 20, ColorMixer.getRed(drawer.getRTAColor()) / 255.0f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableText("speedrunigt.option.timer_position.color_red", "RTA").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getRTAColor()))).asFormattedString());
                    }

                    @Override
                    public void applyValue(float value) {
                        int color = drawer.getRTAColor();
                        drawer.setRTAColor(
                                ColorMixer.getArgb(
                                        ColorMixer.getAlpha(color),
                                        (int) (value * 255),
                                        ColorMixer.getGreen(color),
                                        ColorMixer.getBlue(color)
                                )
                        );
                        changed = true;
                    }
                }))
        );

        rtaOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 + 6, 120, 20, ColorMixer.getGreen(drawer.getRTAColor()) / 255.0f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableText("speedrunigt.option.timer_position.color_green", "RTA").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getRTAColor()))).asFormattedString());
                    }

                    @Override
                    public void applyValue(float value) {
                        int color = drawer.getRTAColor();
                        drawer.setRTAColor(
                                ColorMixer.getArgb(
                                        ColorMixer.getAlpha(color),
                                        ColorMixer.getRed(color),
                                        (int) (value * 255),
                                        ColorMixer.getBlue(color)
                                )
                        );
                        changed = true;
                    }
                }))
        );

        rtaOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 + 28, 120, 20, ColorMixer.getBlue(drawer.getRTAColor()) / 255.0f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableText("speedrunigt.option.timer_position.color_blue", "RTA").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getRTAColor()))).asFormattedString());
                    }

                    @Override
                    public void applyValue(float value) {
                        int color = drawer.getRTAColor();
                        drawer.setRTAColor(
                                ColorMixer.getArgb(
                                        ColorMixer.getAlpha(color),
                                        ColorMixer.getRed(color),
                                        ColorMixer.getGreen(color),
                                        (int) (value * 255)
                                )
                        );
                        changed = true;
                    }
                }))
        );

        rtaOptions.add(
                addButton(new CustomSliderWidget(width / 2 + 6, height / 2 - 16, 120, 20, drawer.getRTAScale() / 3f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableText("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append(((int) (drawer.getRTAScale() * 100)) + "%").asFormattedString());
                    }

                    @Override
                    public void applyValue(float value) {
                        drawer.setRTAScale(Math.round(value * 3f * 20f)/20f);
                        changed = true;
                    }
                }))
        );

        rtaOptions.add(
                addButton(new ConsumerButtonWidget(width / 2 + 6, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_decorate", "RTA").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))).asFormattedString(), (button) -> {
                    int order = drawer.getRTADecoration().ordinal();
                    drawer.setRTADecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    changed = true;
                    button.message = (new TranslatableText("speedrunigt.option.timer_position.text_decorate", "RTA").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))).asFormattedString());
                }))
        );
    }

    public void initBackgroundButtons() {
        backgroundOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 80, height / 2 - 16, 160, 20, drawer.getBGOpacity(), new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableText("speedrunigt.option.timer_position.background_opacity").append(" : ").append((int) (drawer.getBGOpacity()*100) + "%").asFormattedString());
                    }

                    @Override
                    public void applyValue(float value) {
                        drawer.setBGOpacity(value);
                        changed = true;
                    }
                }))
        );

        backgroundOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 80, height / 2 + 6, 160, 20, (drawer.getRTAPadding()-1) / 24f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableText("speedrunigt.option.timer_position.background_padding", "RTA").append(" : ").append(String.valueOf(drawer.getRTAPadding())).asFormattedString());
                    }

                    @Override
                    public void applyValue(float value) {
                        drawer.setRTAPadding((int) (value * 24) + 1);
                        changed = true;
                    }
                }))
        );

        backgroundOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 80, height / 2 + 28, 160, 20,(drawer.getIGTPadding()-1) / 24f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableText("speedrunigt.option.timer_position.background_padding", "IGT").append(" : ").append(String.valueOf(drawer.getIGTPadding())).asFormattedString());
                    }

                    @Override
                    public void applyValue(float value) {
                        drawer.setIGTPadding((int) (value * 24) + 1);
                        changed = true;
                    }
                }))
        );
    }
}
