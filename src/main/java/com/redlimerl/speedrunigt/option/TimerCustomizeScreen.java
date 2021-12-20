package com.redlimerl.speedrunigt.option;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import net.minecraft.client.gui.hud.BackgroundHelper.ColorMixer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

import static com.redlimerl.speedrunigt.option.SpeedRunOptions.*;

public class TimerCustomizeScreen extends Screen {

    private final TimerDrawer drawer = new TimerDrawer(false);
    private final Screen parent;

    private boolean changed = false;
    private boolean hide = false;
    private final ArrayList<ClickableWidget> normalOptions = new ArrayList<>();
    private final ArrayList<ClickableWidget> igtOptions = new ArrayList<>();
    private final ArrayList<ClickableWidget> rtaOptions = new ArrayList<>();
    private ButtonWidget normalButton;
    private ButtonWidget igtButton;
    private ButtonWidget rtaButton;
    private ButtonWidget hideButton;
    private ButtonWidget saveButton;

    public TimerCustomizeScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.option.timer_position"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        initNormal();
        initIGTButtons();
        initRTAButtons();

        this.normalButton = addDrawableChild(new ButtonWidget(width / 2 - 89, height / 2 - 48, 58, 20, new TranslatableText("options.title").append("..."), (ButtonWidget button) -> {
            this.normalButton.active = false;
            this.igtButton.active = true;
            this.rtaButton.active = true;
            this.hideButton.active = false;
            for (ClickableWidget normalOption : normalOptions) {
                normalOption.visible = true;
            }
            for (ClickableWidget igtOption : igtOptions) {
                igtOption.visible = false;
            }
            for (ClickableWidget rtaOption : rtaOptions) {
                rtaOption.visible = false;
            }
        }));
        this.normalButton.active = false;

        this.igtButton = addDrawableChild(new ButtonWidget(width / 2 - 29, height / 2 - 48, 58, 20, new LiteralText("IGT..."), (ButtonWidget button) -> {
            this.normalButton.active = true;
            this.igtButton.active = false;
            this.rtaButton.active = true;
            this.hideButton.active = true;
            for (ClickableWidget normalOption : normalOptions) {
                normalOption.visible = false;
            }
            for (ClickableWidget igtOption : igtOptions) {
                igtOption.visible = true;
            }
            for (ClickableWidget rtaOption : rtaOptions) {
                rtaOption.visible = false;
            }
        }));

        this.rtaButton = addDrawableChild(new ButtonWidget(width / 2 + 31, height / 2 - 48, 58, 20, new LiteralText("RTA..."), (ButtonWidget button) -> {
            this.normalButton.active = true;
            this.igtButton.active = true;
            this.rtaButton.active = false;
            this.hideButton.active = true;
            for (ClickableWidget normalOption : normalOptions) {
                normalOption.visible = false;
            }
            for (ClickableWidget igtOption : igtOptions) {
                igtOption.visible = false;
            }
            for (ClickableWidget rtaOption : rtaOptions) {
                rtaOption.visible = true;
            }
        }));

        this.hideButton = addDrawableChild(new ButtonWidget(width / 2 - 89, height / 2 + 62, 58, 20, new TranslatableText("speedrunigt.option.hide"), (ButtonWidget button) -> {
            hide = !hide;
            for (ClickableWidget normalOption : normalOptions) {
                normalOption.visible = !hide && !normalButton.active;
            }
            for (ClickableWidget igtOption : igtOptions) {
                igtOption.visible = !hide && !igtButton.active;
            }
            for (ClickableWidget rtaOption : rtaOptions) {
                rtaOption.visible = !hide && !rtaButton.active;
            }
            button.setMessage(new TranslatableText("speedrunigt.option." + (!hide ? "hide" : "show")));
        }));
        hideButton.active = false;

        this.saveButton = addDrawableChild(new ButtonWidget(width / 2 - 29, height / 2 + 62, 58, 20, new TranslatableText("selectWorld.edit.save"), (ButtonWidget button) -> {
            setOption(TIMER_IGT_POSITION_X, drawer.getIGT_XPos());
            SpeedRunIGT.TIMER_DRAWER.setIGT_XPos(drawer.getIGT_XPos());
            setOption(TIMER_IGT_POSITION_Y, drawer.getIGT_YPos());
            SpeedRunIGT.TIMER_DRAWER.setIGT_YPos(drawer.getIGT_YPos());
            setOption(TIMER_IGT_SCALE, drawer.getIGTScale());
            SpeedRunIGT.TIMER_DRAWER.setIGTScale(drawer.getIGTScale());
            setOption(TIMER_IGT_COLOR, drawer.getIGTColor());
            SpeedRunIGT.TIMER_DRAWER.setIGTColor(drawer.getIGTColor());
            setOption(TIMER_IGT_OUTLINE, drawer.isIGTDrawOutline());
            SpeedRunIGT.TIMER_DRAWER.setIGTDrawOutline(drawer.isIGTDrawOutline());

            setOption(TIMER_RTA_POSITION_X, drawer.getRTA_XPos());
            SpeedRunIGT.TIMER_DRAWER.setRTA_XPos(drawer.getRTA_XPos());
            setOption(TIMER_RTA_POSITION_Y, drawer.getRTA_YPos());
            SpeedRunIGT.TIMER_DRAWER.setRTA_YPos(drawer.getRTA_YPos());
            setOption(TIMER_RTA_SCALE, drawer.getRTAScale());
            SpeedRunIGT.TIMER_DRAWER.setRTAScale(drawer.getRTAScale());
            setOption(TIMER_RTA_COLOR, drawer.getRTAColor());
            SpeedRunIGT.TIMER_DRAWER.setRTAColor(drawer.getRTAColor());
            setOption(TIMER_RTA_OUTLINE, drawer.isRTADrawOutline());
            SpeedRunIGT.TIMER_DRAWER.setRTADrawOutline(drawer.isRTADrawOutline());

            setOption(DISPLAY_TIME_ONLY, drawer.isSimplyTimer());
            SpeedRunIGT.TIMER_DRAWER.setSimplyTimer(drawer.isSimplyTimer());
            setOption(LOCK_TIMER_POSITION, drawer.isLocked());
            SpeedRunIGT.TIMER_DRAWER.setLocked(drawer.isLocked());
            setOption(DISPLAY_DECIMALS, drawer.getTimerDecimals());
            SpeedRunIGT.TIMER_DRAWER.setTimerDecimals(drawer.getTimerDecimals());

            changed = false;
        }));

        addDrawableChild(new ButtonWidget(width / 2 + 31, height / 2 + 62, 58, 20, ScreenTexts.CANCEL, (ButtonWidget button) -> {
            if (client != null) client.setScreen(parent);
        }));


        for (ClickableWidget normalOption : normalOptions) {
            normalOption.visible = true;
        }
        for (ClickableWidget igtOption : igtOptions) {
            igtOption.visible = false;
        }
        for (ClickableWidget rtaOption : rtaOptions) {
            rtaOption.visible = false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean isClicked = super.mouseClicked(mouseX, mouseY, button);
        if (!isClicked && button == 0 && !drawer.isLocked()) {
            if (!this.igtButton.active) {
                drawer.setIGT_XPos(MathHelper.clamp((float) (mouseX / width), 0, 1));
                drawer.setIGT_YPos(MathHelper.clamp((float) (mouseY / height), 0, 1));
                changed = true;
            }
            if (!this.rtaButton.active) {
                drawer.setRTA_XPos(MathHelper.clamp((float) (mouseX / width), 0, 1));
                drawer.setRTA_YPos(MathHelper.clamp((float) (mouseY / height), 0, 1));
                changed = true;
            }
        }
        return isClicked;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (modifiers == 2 && keyCode >= 262 && keyCode <= 265 && client != null && !drawer.isLocked()) {
            int moveX = keyCode == 262 ? 1 : keyCode == 263 ? -1 : 0;
            int moveY = keyCode == 265 ? -1 : keyCode == 264 ? 1 : 0;
            if (!igtButton.active) {
                drawer.setIGT_XPos(MathHelper.clamp(drawer.getIGT_XPos() + moveX * drawer.getIGTScale() / client.getWindow().getScaledWidth(), 0, 1));
                drawer.setIGT_YPos(MathHelper.clamp(drawer.getIGT_YPos() + moveY * drawer.getIGTScale() / client.getWindow().getScaledHeight(), 0, 1));
                changed = true;
            }
            if (!rtaButton.active) {
                drawer.setRTA_XPos(MathHelper.clamp(drawer.getRTA_XPos() + moveX * drawer.getRTAScale() / client.getWindow().getScaledWidth(), 0, 1));
                drawer.setRTA_YPos(MathHelper.clamp(drawer.getRTA_YPos() + moveY * drawer.getRTAScale() / client.getWindow().getScaledHeight(), 0, 1));
                changed = true;
            }
            setFocused(null);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        saveButton.active = changed;

        this.renderBackground(matrices);

        drawer.draw();

        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
        if (!hide && normalButton.active) {
            if (drawer.isLocked()) {
                drawCenteredText(matrices, this.textRenderer,
                        new TranslatableText("speedrunigt.option.timer_position.description.lock"), this.width / 2, this.height / 2 - 80, 16777215);
            } else {
                drawCenteredText(matrices, this.textRenderer,
                        new TranslatableText("speedrunigt.option.timer_position.description"), this.width / 2, this.height / 2 - 80, 16777215);
                drawCenteredText(matrices, this.textRenderer,
                        new TranslatableText("speedrunigt.option.timer_position.description.move"), this.width / 2, this.height / 2 - 69, 16777215);
            }
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        assert client != null;
        client.setScreen(parent);
    }


    public void initNormal() {
        normalOptions.add(
                addDrawableChild(new ButtonWidget(width / 2 - 80, height / 2 - 16, 160, 20, new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
                    drawer.setSimplyTimer(!drawer.isSimplyTimer());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF));
                }))
        );

        normalOptions.add(
                addDrawableChild(new ButtonWidget(width / 2 - 80, height / 2 + 6, 160, 20, new TranslatableText("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
                    drawer.setLocked(!drawer.isLocked());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF));
                }))
        );

        normalOptions.add(
                addDrawableChild(new ButtonWidget(width / 2 - 80, height / 2 + 28, 160, 20, new TranslatableText("speedrunigt.option.timer_position.show_decimals").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.show_decimals.context", drawer.getTimerDecimals().getNumber())), (ButtonWidget button) -> {
                    int order = drawer.getTimerDecimals().ordinal();
                    drawer.setTimerDecimals(TimerDecimals.values()[(++order) % TimerDecimals.values().length]);
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.show_decimals").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.show_decimals.context", drawer.getTimerDecimals().getNumber())));
                }, (button, matrices, mouseX, mouseY) -> renderTooltip(matrices, new TranslatableText("speedrunigt.option.timer_position.show_decimals.description"), mouseX, mouseY)))
        );
    }

    public void initIGTButtons() {
        igtOptions.add(
                addDrawableChild(new SliderWidget(width / 2 - 127, height / 2 - 16, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_red", "IGT").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getIGTColor()))), ColorMixer.getRed(drawer.getIGTColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_red", "IGT").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getIGTColor()))));
                    }

                    @Override
                    protected void applyValue() {
                        int color = drawer.getIGTColor();
                        drawer.setIGTColor(
                                ColorMixer.getArgb(
                                        ColorMixer.getAlpha(color),
                                        (int) (this.value * 255),
                                        ColorMixer.getGreen(color),
                                        ColorMixer.getBlue(color)
                                )
                        );
                        changed = true;
                    }
                })
        );

        igtOptions.add(
                addDrawableChild(new SliderWidget(width / 2 - 127, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_green", "IGT").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getIGTColor()))), ColorMixer.getGreen(drawer.getIGTColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_green", "IGT").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getIGTColor()))));
                    }

                    @Override
                    protected void applyValue() {
                        int color = drawer.getIGTColor();
                        drawer.setIGTColor(
                                ColorMixer.getArgb(
                                        ColorMixer.getAlpha(color),
                                        ColorMixer.getRed(color),
                                        (int) (this.value * 255),
                                        ColorMixer.getBlue(color)
                                )
                        );
                        changed = true;
                    }
                })
        );

        igtOptions.add(
                addDrawableChild(new SliderWidget(width / 2 - 127, height / 2 + 28, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_blue", "IGT").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getIGTColor()))), ColorMixer.getBlue(drawer.getIGTColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_blue", "IGT").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getIGTColor()))));
                    }

                    @Override
                    protected void applyValue() {
                        int color = drawer.getIGTColor();
                        drawer.setIGTColor(
                                ColorMixer.getArgb(
                                        ColorMixer.getAlpha(color),
                                        ColorMixer.getRed(color),
                                        ColorMixer.getGreen(color),
                                        (int) (this.value * 255)
                                )
                        );
                        changed = true;
                    }
                })
        );

        igtOptions.add(
                addDrawableChild(new SliderWidget(width / 2 + 6, height / 2 - 16, 120, 20, new TranslatableText("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append(((int) (drawer.getIGTScale() * 100)) + "%"), drawer.getIGTScale() / 3f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append(((int) (drawer.getIGTScale() * 100)) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setIGTScale(Math.round((float) this.value * 3f * 20f)/20f);
                        changed = true;
                    }
                })
        );

        igtOptions.add(
                addDrawableChild(new ButtonWidget(width / 2 + 6, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_outline", "IGT").append(" : ").append(drawer.isIGTDrawOutline() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
                    drawer.setIGTDrawOutline(!drawer.isIGTDrawOutline());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.text_outline", "IGT").append(" : ").append(drawer.isIGTDrawOutline() ? ScreenTexts.ON : ScreenTexts.OFF));
                }))
        );
    }

    public void initRTAButtons() {
        rtaOptions.add(
                addDrawableChild(new SliderWidget(width / 2 - 127, height / 2 - 16, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_red", "RTA").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getRTAColor()))), ColorMixer.getRed(drawer.getRTAColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_red", "RTA").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getRTAColor()))));
                    }

                    @Override
                    protected void applyValue() {
                        int color = drawer.getRTAColor();
                        drawer.setRTAColor(
                                ColorMixer.getArgb(
                                        ColorMixer.getAlpha(color),
                                        (int) (this.value * 255),
                                        ColorMixer.getGreen(color),
                                        ColorMixer.getBlue(color)
                                )
                        );
                        changed = true;
                    }
                })
        );

        rtaOptions.add(
                addDrawableChild(new SliderWidget(width / 2 - 127, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_green", "RTA").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getRTAColor()))), ColorMixer.getGreen(drawer.getRTAColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_green", "RTA").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getRTAColor()))));
                    }

                    @Override
                    protected void applyValue() {
                        int color = drawer.getRTAColor();
                        drawer.setRTAColor(
                                ColorMixer.getArgb(
                                        ColorMixer.getAlpha(color),
                                        ColorMixer.getRed(color),
                                        (int) (this.value * 255),
                                        ColorMixer.getBlue(color)
                                )
                        );
                        changed = true;
                    }
                })
        );

        rtaOptions.add(
                addDrawableChild(new SliderWidget(width / 2 - 127, height / 2 + 28, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_blue", "RTA").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getRTAColor()))), ColorMixer.getBlue(drawer.getRTAColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_blue", "RTA").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getRTAColor()))));
                    }

                    @Override
                    protected void applyValue() {
                        int color = drawer.getRTAColor();
                        drawer.setRTAColor(
                                ColorMixer.getArgb(
                                        ColorMixer.getAlpha(color),
                                        ColorMixer.getRed(color),
                                        ColorMixer.getGreen(color),
                                        (int) (this.value * 255)
                                )
                        );
                        changed = true;
                    }
                })
        );

        rtaOptions.add(
                addDrawableChild(new SliderWidget(width / 2 + 6, height / 2 - 16, 120, 20, new TranslatableText("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append(((int) (drawer.getRTAScale() * 100)) + "%"), drawer.getRTAScale() / 3f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append(((int) (drawer.getRTAScale() * 100)) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setRTAScale(Math.round((float) this.value * 3f * 20f)/20f);
                        changed = true;
                    }
                })
        );

        rtaOptions.add(
                addDrawableChild(new ButtonWidget(width / 2 + 6, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_outline", "RTA").append(" : ").append(drawer.isRTADrawOutline() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
                    drawer.setRTADrawOutline(!drawer.isRTADrawOutline());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.text_outline", "RTA").append(" : ").append(drawer.isRTADrawOutline() ? ScreenTexts.ON : ScreenTexts.OFF));
                }))
        );
    }

}
