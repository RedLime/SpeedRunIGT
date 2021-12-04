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
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;

public class TimerCustomizeScreen extends Screen {

    private final TimerDrawer drawer = new TimerDrawer(false);
    private final Screen parent;

    private boolean changed = false;
    private int page = 0;
    private final ArrayList<ClickableWidget> normalOptions = new ArrayList<>();
    private final ArrayList<ClickableWidget> colorOptions = new ArrayList<>();
    private ButtonWidget saveButton;

    public TimerCustomizeScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.option.timer_position"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        addDrawableChild(new ButtonWidget(width / 2 - 60, height / 2 - 48, 120, 20, new TranslatableText("speedrunigt.option.timer_position."+ (page == 0 ? "color_mode" : "position_mode")), (ButtonWidget button) -> {
            if (page == 0) page = 1;
            else page = 0;

            for (ClickableWidget colorOption : colorOptions) {
                colorOption.visible = page == 1;
            }
            for (ClickableWidget normalOption : normalOptions) {
                normalOption.visible = page == 0;
            }
            button.setMessage(new TranslatableText("speedrunigt.option.timer_position." + (page == 0 ? "color_mode" : "position_mode")));
        }));

        initNormal();
        initColor();

        this.saveButton = addDrawableChild(new ButtonWidget(width / 2 - 60, height / 2 + 62, 58, 20, new TranslatableText("selectWorld.edit.save"), (ButtonWidget button) -> {
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_POSITION_X, drawer.getXPos());
            SpeedRunIGT.TIMER_DRAWER.setXPos(drawer.getXPos());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_POSITION_Y, drawer.getYPos());
            SpeedRunIGT.TIMER_DRAWER.setYPos(drawer.getYPos());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_SCALE, drawer.getIGTScale());
            SpeedRunIGT.TIMER_DRAWER.setIGTScale(drawer.getIGTScale());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_SCALE, drawer.getRTAScale());
            SpeedRunIGT.TIMER_DRAWER.setRTAScale(drawer.getRTAScale());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_BG_OPACITY, drawer.getBackgroundOpacity());
            SpeedRunIGT.TIMER_DRAWER.setBackgroundOpacity(drawer.getBackgroundOpacity());
            SpeedRunOptions.setOption(SpeedRunOptions.REVERSED_IGT_RTA, drawer.isReversedOrder());
            SpeedRunIGT.TIMER_DRAWER.setReversedOrder(drawer.isReversedOrder());
            SpeedRunOptions.setOption(SpeedRunOptions.DISPLAY_TIME_ONLY, drawer.isSimplyTimer());
            SpeedRunIGT.TIMER_DRAWER.setSimplyTimer(drawer.isSimplyTimer());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_COLOR, drawer.getIGTColor());
            SpeedRunIGT.TIMER_DRAWER.setIGTColor(drawer.getIGTColor());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_IGT_OUTLINE, drawer.isIGTDrawOutline());
            SpeedRunIGT.TIMER_DRAWER.setIGTDrawOutline(drawer.isIGTDrawOutline());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_COLOR, drawer.getRTAColor());
            SpeedRunIGT.TIMER_DRAWER.setRTAColor(drawer.getRTAColor());
            SpeedRunOptions.setOption(SpeedRunOptions.TIMER_RTA_OUTLINE, drawer.isRTADrawOutline());
            SpeedRunIGT.TIMER_DRAWER.setRTADrawOutline(drawer.isRTADrawOutline());
            changed = false;
        }));

        addDrawableChild(new ButtonWidget(width / 2 + 1, height / 2 + 62, 58, 20, ScreenTexts.CANCEL, (ButtonWidget button) -> {
            if (client != null) client.setScreen(parent);
        }));


        for (ClickableWidget colorOption : colorOptions) {
            colorOption.visible = false;
        }
        for (ClickableWidget normalOption : normalOptions) {
            normalOption.visible = true;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean isClicked = super.mouseClicked(mouseX, mouseY, button);
        if (!isClicked) {
            drawer.setXPos((float) (mouseX / width));
            drawer.setYPos((float) (mouseY / height));
            changed = true;
        }
        return isClicked;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        saveButton.active = changed;

        this.renderBackground(matrices);

        drawer.draw();

        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
        drawCenteredText(matrices, this.textRenderer,
                new TranslatableText("speedrunigt.option.timer_position.description"), this.width / 2, this.height / 2 - 62, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        assert client != null;
        client.setScreen(parent);
    }

    public void initNormal() {
        normalOptions.add(
                addDrawableChild(new SliderWidget(width / 2 - 127, height / 2 - 26, 120, 20, new TranslatableText("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append(((int) (drawer.getIGTScale() * 100)) + "%"), (drawer.getIGTScale() - 0.5f) / 2.5f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append(((int) (drawer.getIGTScale() * 100)) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setIGTScale(Math.round((float) (0.5f + (this.value * 2.5f)) * 20f)/20f);
                        changed = true;
                    }
                })
        );
        normalOptions.add(
                addDrawableChild(new SliderWidget(width / 2 + 6, height / 2 - 26, 120, 20, new TranslatableText("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append(((int) (drawer.getRTAScale() * 100)) + "%"), (drawer.getRTAScale() - 0.5f) / 2.5f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append(((int) (drawer.getRTAScale() * 100)) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setRTAScale(Math.round((float) (0.5f + (this.value * 2.5f)) * 20f)/20f);
                        changed = true;
                    }
                })
        );

        normalOptions.add(
                addDrawableChild(new SliderWidget(width / 2 - 60, height / 2 - 4, 120, 20, new TranslatableText("speedrunigt.option.timer_position.background_opacity").append(" : ").append(((int) (drawer.getBackgroundOpacity() * 100)) + "%"), drawer.getBackgroundOpacity()) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.background_opacity").append(" : ").append(((int) (drawer.getBackgroundOpacity() * 100)) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setBackgroundOpacity((float) this.value);
                        changed = true;
                    }
                })
        );

        normalOptions.add(
                addDrawableChild(new ButtonWidget(width / 2 - 60, height / 2 + 18, 120, 20, new TranslatableText("speedrunigt.option.timer_position.top_timer").append(" : ").append(drawer.isReversedOrder() ?  "RTA" : "IGT"), (ButtonWidget button) -> {
                    drawer.setReversedOrder(!drawer.isReversedOrder());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.top_timer").append(" : ").append(drawer.isReversedOrder() ?  "RTA" : "IGT"));
                }))
        );

        normalOptions.add(
                addDrawableChild(new ButtonWidget(width / 2 - 60, height / 2 + 40, 120, 20, new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
                    drawer.setSimplyTimer(!drawer.isSimplyTimer());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF));
                }))
        );
    }

    public void initColor() {
        colorOptions.add(
                addDrawableChild(new SliderWidget(width / 2 - 127, height / 2 - 26, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_red", "IGT").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getIGTColor()))), ColorMixer.getRed(drawer.getIGTColor()) / 255.0f) {
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

        colorOptions.add(
                addDrawableChild(new SliderWidget(width / 2 - 127, height / 2 - 4, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_green", "IGT").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getIGTColor()))), ColorMixer.getGreen(drawer.getIGTColor()) / 255.0f) {
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

        colorOptions.add(
                addDrawableChild(new SliderWidget(width / 2 - 127, height / 2 + 18, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_blue", "IGT").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getIGTColor()))), ColorMixer.getBlue(drawer.getIGTColor()) / 255.0f) {
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

        colorOptions.add(
                addDrawableChild(new SliderWidget(width / 2 + 6, height / 2 - 26, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_red", "RTA").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getRTAColor()))), ColorMixer.getRed(drawer.getRTAColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_red", "IGT").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getRTAColor()))));
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

        colorOptions.add(
                addDrawableChild(new SliderWidget(width / 2 + 6, height / 2 - 4, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_green", "RTA").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getRTAColor()))), ColorMixer.getGreen(drawer.getRTAColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_green", "IGT").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getRTAColor()))));
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

        colorOptions.add(
                addDrawableChild(new SliderWidget(width / 2 + 6, height / 2 + 18, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_blue", "RTA").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getRTAColor()))), ColorMixer.getBlue(drawer.getRTAColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_blue", "IGT").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getRTAColor()))));
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

        colorOptions.add(
                addDrawableChild(new ButtonWidget(width / 2 + 6, height / 2 + 40, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_outline", "RTA").append(" : ").append(drawer.isRTADrawOutline() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
                    drawer.setRTADrawOutline(!drawer.isRTADrawOutline());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.text_outline", "RTA").append(" : ").append(drawer.isRTADrawOutline() ? ScreenTexts.ON : ScreenTexts.OFF));
                }))
        );

        colorOptions.add(
                addDrawableChild(new ButtonWidget(width / 2 - 127, height / 2 + 40, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_outline", "IGT").append(" : ").append(drawer.isIGTDrawOutline() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
                    drawer.setIGTDrawOutline(!drawer.isIGTDrawOutline());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.text_outline", "IGT").append(" : ").append(drawer.isIGTDrawOutline() ? ScreenTexts.ON : ScreenTexts.OFF));
                }))
        );
    }

}
