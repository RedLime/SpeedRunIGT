package com.redlimerl.speedrunigt.option;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecimals;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.version.CustomSliderWidget;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;

public class TimerCustomizeScreen extends Screen {

    private final TimerDrawer drawer = new TimerDrawer(false);
    private final Screen parent;

    private boolean changed = false;
    private boolean hide = false;
    private final ArrayList<AbstractButtonWidget> normalOptions = new ArrayList<>();
    private final ArrayList<AbstractButtonWidget> igtOptions = new ArrayList<>();
    private final ArrayList<AbstractButtonWidget> rtaOptions = new ArrayList<>();
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

        this.normalButton = addButton(new ButtonWidget(width / 2 - 89, height / 2 - 48, 58, 20, new TranslatableText("options.title").append("...").asFormattedString(), (ButtonWidget button) -> {
            this.normalButton.active = false;
            this.igtButton.active = true;
            this.rtaButton.active = true;
            this.hideButton.active = false;
            for (AbstractButtonWidget normalOption : normalOptions) {
                normalOption.visible = true;
            }
            for (AbstractButtonWidget igtOption : igtOptions) {
                igtOption.visible = false;
            }
            for (AbstractButtonWidget rtaOption : rtaOptions) {
                rtaOption.visible = false;
            }
        }));
        this.normalButton.active = false;

        this.igtButton = addButton(new ButtonWidget(width / 2 - 29, height / 2 - 48, 58, 20, new LiteralText("IGT...").asFormattedString(), (ButtonWidget button) -> {
            this.normalButton.active = true;
            this.igtButton.active = false;
            this.rtaButton.active = true;
            this.hideButton.active = true;
            for (AbstractButtonWidget normalOption : normalOptions) {
                normalOption.visible = false;
            }
            for (AbstractButtonWidget igtOption : igtOptions) {
                igtOption.visible = true;
            }
            for (AbstractButtonWidget rtaOption : rtaOptions) {
                rtaOption.visible = false;
            }
        }));

        this.rtaButton = addButton(new ButtonWidget(width / 2 + 31, height / 2 - 48, 58, 20, new LiteralText("RTA...").asFormattedString(), (ButtonWidget button) -> {
            this.normalButton.active = true;
            this.igtButton.active = true;
            this.rtaButton.active = false;
            this.hideButton.active = true;
            for (AbstractButtonWidget normalOption : normalOptions) {
                normalOption.visible = false;
            }
            for (AbstractButtonWidget igtOption : igtOptions) {
                igtOption.visible = false;
            }
            for (AbstractButtonWidget rtaOption : rtaOptions) {
                rtaOption.visible = true;
            }
        }));

        this.hideButton = addButton(new ButtonWidget(width / 2 - 89, height / 2 + 62, 58, 20, new TranslatableText("speedrunigt.option.hide").asFormattedString(), (ButtonWidget button) -> {
            hide = !hide;
            for (AbstractButtonWidget normalOption : normalOptions) {
                normalOption.visible = !hide && !normalButton.active;
            }
            for (AbstractButtonWidget igtOption : igtOptions) {
                igtOption.visible = !hide && !igtButton.active;
            }
            for (AbstractButtonWidget rtaOption : rtaOptions) {
                rtaOption.visible = !hide && !rtaButton.active;
            }
            button.setMessage(new TranslatableText("speedrunigt.option." + (!hide ? "hide" : "show")).asFormattedString());
        }));
        hideButton.active = false;

        this.saveButton = addButton(new ButtonWidget(width / 2 - 29, height / 2 + 62, 58, 20, new TranslatableText("selectWorld.edit.save").asFormattedString(), (ButtonWidget button) -> {
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_POSITION_X, drawer.getIGT_XPos());
            SpeedRunIGT.TIMER_DRAWER.setIGT_XPos(drawer.getIGT_XPos());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_POSITION_Y, drawer.getIGT_YPos());
            SpeedRunIGT.TIMER_DRAWER.setIGT_YPos(drawer.getIGT_YPos());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_SCALE, drawer.getIGTScale());
            SpeedRunIGT.TIMER_DRAWER.setIGTScale(drawer.getIGTScale());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_COLOR, drawer.getIGTColor());
            SpeedRunIGT.TIMER_DRAWER.setIGTColor(drawer.getIGTColor());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_OUTLINE, drawer.isIGTDrawOutline());
            SpeedRunIGT.TIMER_DRAWER.setIGTDrawOutline(drawer.isIGTDrawOutline());

            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_POSITION_X, drawer.getRTA_XPos());
            SpeedRunIGT.TIMER_DRAWER.setRTA_XPos(drawer.getRTA_XPos());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_POSITION_Y, drawer.getRTA_YPos());
            SpeedRunIGT.TIMER_DRAWER.setRTA_YPos(drawer.getRTA_YPos());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_SCALE, drawer.getRTAScale());
            SpeedRunIGT.TIMER_DRAWER.setRTAScale(drawer.getRTAScale());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_COLOR, drawer.getRTAColor());
            SpeedRunIGT.TIMER_DRAWER.setRTAColor(drawer.getRTAColor());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_OUTLINE, drawer.isRTADrawOutline());
            SpeedRunIGT.TIMER_DRAWER.setRTADrawOutline(drawer.isRTADrawOutline());

            SpeedRunOptions.setOption(SpeedRunOptions.DISPLAY_TIME_ONLY, drawer.isSimplyTimer());
            SpeedRunIGT.TIMER_DRAWER.setSimplyTimer(drawer.isSimplyTimer());
            SpeedRunOptions.setOption(SpeedRunOptions.LOCK_TIMER_POSITION, drawer.isLocked());
            SpeedRunIGT.TIMER_DRAWER.setLocked(drawer.isLocked());
            SpeedRunOptions.setOption(SpeedRunOptions.DISPLAY_DECIMALS, drawer.getTimerDecimals());
            SpeedRunIGT.TIMER_DRAWER.setTimerDecimals(drawer.getTimerDecimals());

            changed = false;
        }));

        addButton(new ButtonWidget(width / 2 + 31, height / 2 + 62, 58, 20, ScreenTexts.CANCEL, (ButtonWidget button) -> {
            if (minecraft != null) minecraft.openScreen(parent);
        }));


        for (AbstractButtonWidget normalOption : normalOptions) {
            normalOption.visible = true;
        }
        for (AbstractButtonWidget igtOption : igtOptions) {
            igtOption.visible = false;
        }
        for (AbstractButtonWidget rtaOption : rtaOptions) {
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
        if (modifiers == 2 && keyCode >= 262 && keyCode <= 265 && minecraft != null && !drawer.isLocked()) {
            int moveX = keyCode == 262 ? 1 : keyCode == 263 ? -1 : 0;
            int moveY = keyCode == 265 ? -1 : keyCode == 264 ? 1 : 0;
            if (!igtButton.active) {
                drawer.setIGT_XPos(MathHelper.clamp(drawer.getIGT_XPos() + moveX * drawer.getIGTScale() / minecraft.window.getScaledWidth(), 0, 1));
                drawer.setIGT_YPos(MathHelper.clamp(drawer.getIGT_YPos() + moveY * drawer.getIGTScale() / minecraft.window.getScaledHeight(), 0, 1));
                changed = true;
            }
            if (!rtaButton.active) {
                drawer.setRTA_XPos(MathHelper.clamp(drawer.getRTA_XPos() + moveX * drawer.getRTAScale() / minecraft.window.getScaledWidth(), 0, 1));
                drawer.setRTA_YPos(MathHelper.clamp(drawer.getRTA_YPos() + moveY * drawer.getRTAScale() / minecraft.window.getScaledHeight(), 0, 1));
                changed = true;
            }
            setFocused(null);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        saveButton.active = changed;

        this.renderBackground();

        drawer.draw();

        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 15, 16777215);
        if (!hide && normalButton.active) {
            if (drawer.isLocked()) {
                drawCenteredString(this.font,
                        I18n.translate("speedrunigt.option.timer_position.description.lock"), this.width / 2, this.height / 2 - 80, 16777215);
            } else {
                drawCenteredString(this.font,
                        I18n.translate("speedrunigt.option.timer_position.description"), this.width / 2, this.height / 2 - 80, 16777215);
                drawCenteredString(this.font,
                        I18n.translate("speedrunigt.option.timer_position.description.move"), this.width / 2, this.height / 2 - 69, 16777215);
            }
        }
        super.render(mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        minecraft.openScreen(parent);
    }


    public void initNormal() {
        normalOptions.add(
                addButton(new ButtonWidget(width / 2 - 80, height / 2 - 16, 160, 20, new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(), (ButtonWidget button) -> {
                    drawer.setSimplyTimer(!drawer.isSimplyTimer());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                }))
        );

        normalOptions.add(
                addButton(new ButtonWidget(width / 2 - 80, height / 2 + 6, 160, 20, new TranslatableText("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(), (ButtonWidget button) -> {
                    drawer.setLocked(!drawer.isLocked());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                }))
        );

        normalOptions.add(
                addButton(new ButtonWidget(width / 2 - 80, height / 2 + 28, 160, 20, new TranslatableText("speedrunigt.option.timer_position.show_decimals").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.show_decimals.context", drawer.getTimerDecimals().getNumber())).asFormattedString(), (ButtonWidget button) -> {
                    int order = drawer.getTimerDecimals().ordinal();
                    drawer.setTimerDecimals(TimerDecimals.values()[(++order) % TimerDecimals.values().length]);
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.show_decimals").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.show_decimals.context", drawer.getTimerDecimals().getNumber())).asFormattedString());
                }))
        );
    }

    public void initIGTButtons() {
        igtOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 - 16, 120, 20, new Color(drawer.getIGTColor()).getRed() / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_red", "IGT").append(" : ").append(String.valueOf(new Color(drawer.getIGTColor()).getRed())).asFormattedString());
                    }

                    @Override
                    protected void applyValue() {
                        Color color = new Color(drawer.getIGTColor());
                        drawer.setIGTColor(
                                new Color(
                                        color.getAlpha(),
                                        (int) (this.value * 255),
                                        color.getGreen(),
                                        color.getBlue()
                                ).getRGB()
                        );
                        changed = true;
                    }
                })
        );

        igtOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 + 6, 120, 20, new Color(drawer.getIGTColor()).getGreen() / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_green", "IGT").append(" : ").append(String.valueOf(new Color(drawer.getIGTColor()).getGreen())).asFormattedString());
                    }

                    @Override
                    protected void applyValue() {
                        Color color = new Color(drawer.getIGTColor());
                        drawer.setIGTColor(
                                new Color(
                                        color.getAlpha(),
                                        color.getRed(),
                                        (int) (this.value * 255),
                                        color.getBlue()
                                ).getRGB()
                        );
                        changed = true;
                    }
                })
        );

        igtOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 + 28, 120, 20, new Color(drawer.getIGTColor()).getBlue() / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_blue", "IGT").append(" : ").append(String.valueOf(new Color(drawer.getIGTColor()).getBlue())).asFormattedString());
                    }

                    @Override
                    protected void applyValue() {
                        Color color = new Color(drawer.getIGTColor());
                        drawer.setIGTColor(
                                new Color(
                                        color.getAlpha(),
                                        color.getRed(),
                                        color.getGreen(),
                                        (int) (this.value * 255)
                                ).getRGB()
                        );
                        changed = true;
                    }
                })
        );

        igtOptions.add(
                addButton(new CustomSliderWidget(width / 2 + 6, height / 2 - 16, 120, 20, drawer.getIGTScale() / 3f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append(((int) (drawer.getIGTScale() * 100)) + "%").asFormattedString());
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setIGTScale(Math.round((float) this.value * 3f * 20f)/20f);
                        changed = true;
                    }
                })
        );

        igtOptions.add(
                addButton(new ButtonWidget(width / 2 + 6, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_outline", "IGT").append(" : ").append(drawer.isIGTDrawOutline() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(), (ButtonWidget button) -> {
                    drawer.setIGTDrawOutline(!drawer.isIGTDrawOutline());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.text_outline", "IGT").append(" : ").append(drawer.isIGTDrawOutline() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                }))
        );
    }

    public void initRTAButtons() {
        rtaOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 - 16, 120, 20, new Color(drawer.getRTAColor()).getRed() / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_red", "RTA").append(" : ").append(String.valueOf(new Color(drawer.getRTAColor()).getRed())).asFormattedString());
                    }

                    @Override
                    protected void applyValue() {
                        Color color = new Color(drawer.getRTAColor());
                        drawer.setRTAColor(
                                new Color(
                                        color.getAlpha(),
                                        (int) (this.value * 255),
                                        color.getGreen(),
                                        color.getBlue()
                                ).getRGB()
                        );
                        changed = true;
                    }
                })
        );

        rtaOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 + 6, 120, 20, new Color(drawer.getRTAColor()).getGreen() / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_green", "RTA").append(" : ").append(String.valueOf(new Color(drawer.getRTAColor()).getGreen())).asFormattedString());
                    }

                    @Override
                    protected void applyValue() {
                        Color color = new Color(drawer.getRTAColor());
                        drawer.setRTAColor(
                                new Color(
                                        color.getAlpha(),
                                        color.getRed(),
                                        (int) (this.value * 255),
                                        color.getBlue()
                                ).getRGB()
                        );
                        changed = true;
                    }
                })
        );

        rtaOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 + 28, 120, 20, new Color(drawer.getRTAColor()).getBlue() / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_blue", "RTA").append(" : ").append(String.valueOf(new Color(drawer.getRTAColor()).getBlue())).asFormattedString());
                    }

                    @Override
                    protected void applyValue() {
                        Color color = new Color(drawer.getRTAColor());
                        drawer.setRTAColor(
                                new Color(
                                        color.getAlpha(),
                                        color.getRed(),
                                        color.getGreen(),
                                        (int) (this.value * 255)
                                ).getRGB()
                        );
                        changed = true;
                    }
                })
        );

        rtaOptions.add(
                addButton(new CustomSliderWidget(width / 2 + 6, height / 2 - 16, 120, 20, drawer.getRTAScale() / 3f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append(((int) (drawer.getRTAScale() * 100)) + "%").asFormattedString());
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setRTAScale(Math.round((float) this.value * 3f * 20f)/20f);
                        changed = true;
                    }
                })
        );

        rtaOptions.add(
                addButton(new ButtonWidget(width / 2 + 6, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_outline", "RTA").append(" : ").append(drawer.isRTADrawOutline() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(), (ButtonWidget button) -> {
                    drawer.setRTADrawOutline(!drawer.isRTADrawOutline());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.text_outline", "RTA").append(" : ").append(drawer.isRTADrawOutline() ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                }))
        );
    }

}
