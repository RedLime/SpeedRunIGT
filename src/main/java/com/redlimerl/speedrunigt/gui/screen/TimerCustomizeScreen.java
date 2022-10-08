package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.gui.CustomSliderWidget;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecimals;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.timer.TimerDrawer.PositionType;
import com.redlimerl.speedrunigt.utils.Vec2f;
import com.redlimerl.speedrunigt.version.ColorMixer;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.Window;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import java.util.*;

public class TimerCustomizeScreen extends Screen {

    private final TimerDrawer drawer = new TimerDrawer(false);
    private final Screen parent;

    private PositionType currentPosType = PositionType.DEFAULT;
    private final HashMap<PositionType, Vec2f> posTypesRTA = new HashMap<>();
    private final HashMap<PositionType, Vec2f> posTypesIGT = new HashMap<>();

    private boolean changed = false;
    private boolean hide = false;
    private final ArrayList<ButtonWidget> tabButtons = new ArrayList<>();
    private final ArrayList<ButtonWidget> normalOptions = new ArrayList<>();
    private final ArrayList<ButtonWidget> igtOptions = new ArrayList<>();
    private final ArrayList<ButtonWidget> rtaOptions = new ArrayList<>();
    private final ArrayList<ButtonWidget> posOptions = new ArrayList<>();
    private final ArrayList<ButtonWidget> backgroundOptions = new ArrayList<>();
    private ConsumerButtonWidget normalButton;
    private ConsumerButtonWidget igtButton;
    private ConsumerButtonWidget rtaButton;
    private ConsumerButtonWidget posButton;
    private ConsumerButtonWidget backgroundButton;
    private ConsumerButtonWidget saveButton;

    private boolean splitPosition = SpeedRunOption.getOption(SpeedRunOptions.ENABLE_TIMER_SPLIT_POS);

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
        this.posButton.active = tab != 5;
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
        for (ButtonWidget posOption : posOptions) {
            posOption.visible = tab == 5;
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.buttonClicked(button);
    }

    public <T extends ButtonWidget> T addButton(T button) {
        buttons.add(button);
        return button;
    }

    @Override
    public void init() {
        normalOptions.clear();
        igtOptions.clear();
        rtaOptions.clear();
        posOptions.clear();
        backgroundOptions.clear();

        initNormal();
        initIGTButtons();
        initRTAButtons();
        initPositionButtons();
        initBackgroundButtons();

        this.normalButton = new ConsumerButtonWidget(width / 2 - 179, height / 2 - 48, 58, 20, new TranslatableText("options.title").append("...").asFormattedString(), (button) -> openTab(0));
        buttons.add(this.normalButton);
        this.tabButtons.add(this.normalButton);

        this.igtButton = new ConsumerButtonWidget(width / 2 - 119, height / 2 - 48, 58, 20, new LiteralText("IGT...").asFormattedString(), (button) -> openTab(1));
        buttons.add(this.igtButton);
        this.tabButtons.add(this.igtButton);

        this.rtaButton = new ConsumerButtonWidget(width / 2 - 59, height / 2 - 48, 58, 20, new LiteralText("RTA...").asFormattedString(), (button) -> openTab(2));
        buttons.add(this.rtaButton);
        this.tabButtons.add(this.rtaButton);

        this.posButton = addButton(new ConsumerButtonWidget(width / 2 + 1, height / 2 - 48, 58, 20, "Pos...", (ButtonWidget button) -> openTab(5)));
        buttons.add(this.posButton);
        this.tabButtons.add(this.posButton);

        ButtonWidget fontButton = new ConsumerButtonWidget(width / 2 + 61, height / 2 - 48, 58, 20, new TranslatableText("speedrunigt.title.font").asFormattedString(), button -> {});
        fontButton.active = false;
        buttons.add(fontButton);
        this.tabButtons.add(fontButton);

        this.backgroundButton = new ConsumerButtonWidget(width / 2 + 121, height / 2 - 48, 58, 20, new TranslatableText("speedrunigt.title.background").asFormattedString(), (button) -> openTab(4));
        buttons.add(this.backgroundButton);
        this.tabButtons.add(this.backgroundButton);

        buttons.add(new ConsumerButtonWidget(width / 2 - 89, height / 2 + 62, 58, 20, new TranslatableText("speedrunigt.option.hide").asFormattedString(), (button) -> {
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
            for (ButtonWidget posOption : posOptions) {
                posOption.visible = !hide && currentTab == 5;
            }
            for (ButtonWidget backgroundOption : backgroundOptions) {
                backgroundOption.visible = !hide && currentTab == 4;
            }
            for (ButtonWidget tabButton : tabButtons) {
                tabButton.visible = !hide;
            }
            openTab(currentTab);
            button.message = new TranslatableText("speedrunigt.option." + (!hide ? "hide" : "show")).asFormattedString();
        }));

        this.saveButton = new ConsumerButtonWidget(width / 2 - 29, height / 2 + 62, 58, 20, new TranslatableText("speedrunigt.option.save").asFormattedString(), (button) -> {
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_SCALE, drawer.getIGTScale());
            SpeedRunIGTClient.TIMER_DRAWER.setIGTScale(drawer.getIGTScale());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_COLOR, drawer.getIGTColor());
            SpeedRunIGTClient.TIMER_DRAWER.setIGTColor(drawer.getIGTColor());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_DECO, drawer.getIGTDecoration());
            SpeedRunIGTClient.TIMER_DRAWER.setIGTDecoration(drawer.getIGTDecoration());

            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_SCALE, drawer.getRTAScale());
            SpeedRunIGTClient.TIMER_DRAWER.setRTAScale(drawer.getRTAScale());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_COLOR, drawer.getRTAColor());
            SpeedRunIGTClient.TIMER_DRAWER.setRTAColor(drawer.getRTAColor());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_DECO, drawer.getRTADecoration());
            SpeedRunIGTClient.TIMER_DRAWER.setRTADecoration(drawer.getRTADecoration());

            SpeedRunOption.setOption(SpeedRunOptions.DISPLAY_TIME_ONLY, drawer.isSimplyTimer());
            SpeedRunIGTClient.TIMER_DRAWER.setSimplyTimer(drawer.isSimplyTimer());
            SpeedRunOption.setOption(SpeedRunOptions.LOCK_TIMER_POSITION, drawer.isLocked());
            SpeedRunIGTClient.TIMER_DRAWER.setLocked(drawer.isLocked());
            SpeedRunOption.setOption(SpeedRunOptions.DISPLAY_DECIMALS, drawer.getTimerDecimals());
            SpeedRunIGTClient.TIMER_DRAWER.setTimerDecimals(drawer.getTimerDecimals());

            SpeedRunOption.setOption(SpeedRunOptions.BACKGROUND_OPACITY, drawer.getBGOpacity());
            SpeedRunIGTClient.TIMER_DRAWER.setBGOpacity(drawer.getBGOpacity());
            SpeedRunOption.setOption(SpeedRunOptions.RTA_BACKGROUND_PADDING, drawer.getRTAPadding());
            SpeedRunIGTClient.TIMER_DRAWER.setRTAPadding(drawer.getRTAPadding());
            SpeedRunOption.setOption(SpeedRunOptions.IGT_BACKGROUND_PADDING, drawer.getIGTPadding());
            SpeedRunIGTClient.TIMER_DRAWER.setIGTPadding(drawer.getIGTPadding());

            for (Map.Entry<PositionType, Vec2f> igtPosEntry : posTypesIGT.entrySet()) {
                if (igtPosEntry.getKey() == PositionType.DEFAULT) {
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_POSITION_X, igtPosEntry.getValue().x);
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_POSITION_Y, igtPosEntry.getValue().y);
                } else {
                    SpeedRunOption.setOption(igtPosEntry.getKey() == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_IGT_POSITION_FOR_F3 : SpeedRunOptions.TIMER_IGT_POSITION_FOR_PAUSE, igtPosEntry.getValue());
                }
            }

            for (Map.Entry<PositionType, Vec2f> rtaPosEntry : posTypesRTA.entrySet()) {
                if (rtaPosEntry.getKey() == PositionType.DEFAULT) {
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_POSITION_X, rtaPosEntry.getValue().x);
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_POSITION_Y, rtaPosEntry.getValue().y);
                } else {
                    SpeedRunOption.setOption(rtaPosEntry.getKey() == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_RTA_POSITION_FOR_F3 : SpeedRunOptions.TIMER_RTA_POSITION_FOR_PAUSE, rtaPosEntry.getValue());
                }
            }

            SpeedRunIGTClient.TIMER_DRAWER.update();
            SpeedRunOption.setOption(SpeedRunOptions.ENABLE_TIMER_SPLIT_POS, splitPosition);

            changed = false;
        });
        buttons.add(this.saveButton);

        buttons.add(new ConsumerButtonWidget(width / 2 + 31, height / 2 + 62, 58, 20, ScreenTexts.CANCEL, (button) -> onClose()));

        openTab(0);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        boolean isButtonClick = false;
        for (ButtonWidget widget : this.buttons) {
            if (widget.isMouseOver(this.client, mouseX, mouseY)) {
                isButtonClick = true;
            }
        }
        if (button == 0 && !drawer.isLocked()&&!isButtonClick) {
            Window window = new Window(client);
            if (!this.igtButton.active) {
                drawer.setIGT_XPos((float) MathHelper.clamp(mouseX / window.getScaledWidth(), 0, 1));
                drawer.setIGT_YPos((float) MathHelper.clamp(mouseY / window.getScaledHeight(), 0, 1));
                posTypesIGT.put(currentPosType, new Vec2f(drawer.getIGT_XPos(), drawer.getIGT_YPos()));
                changed = true;
            }
            if (!this.rtaButton.active) {
                drawer.setRTA_XPos((float) MathHelper.clamp(mouseX / window.getScaledWidth(), 0, 1));
                drawer.setRTA_YPos((float) MathHelper.clamp(mouseY / window.getScaledHeight(), 0, 1));
                posTypesRTA.put(currentPosType, new Vec2f(drawer.getRTA_XPos(), drawer.getRTA_YPos()));
                changed = true;
            }
        }
        super.mouseClicked(mouseX,mouseY,button);
    }

    @Override
    protected void keyPressed(char character, int keyCode) {
        if (hasControlDown() && keyCode >= 200 && keyCode <= 208 && client != null && !drawer.isLocked()) {
            int moveX = keyCode == 205 ? 1 : keyCode == 203 ? -1 : 0;
            int moveY = keyCode == 200 ? -1 : keyCode == 208 ? 1 : 0;
            Window window = new Window(client);
            if (!igtButton.active) {
                drawer.setIGT_XPos(MathHelper.clamp(drawer.getIGT_XPos() + moveX * drawer.getIGTScale() / (float) window.getScaledWidth(), 0, 1));
                drawer.setIGT_YPos(MathHelper.clamp(drawer.getIGT_YPos() + moveY * drawer.getIGTScale() / (float) window.getScaledHeight(), 0, 1));
                posTypesIGT.put(currentPosType, new Vec2f(drawer.getIGT_XPos(), drawer.getIGT_YPos()));
                changed = true;
            }
            if (!rtaButton.active) {
                drawer.setRTA_XPos(MathHelper.clamp(drawer.getRTA_XPos() + moveX * drawer.getRTAScale() / (float) window.getScaledWidth(), 0, 1));
                drawer.setRTA_YPos(MathHelper.clamp(drawer.getRTA_YPos() + moveY * drawer.getRTAScale() / (float) window.getScaledHeight(), 0, 1));
                posTypesRTA.put(currentPosType, new Vec2f(drawer.getRTA_XPos(), drawer.getRTA_YPos()));
                changed = true;
            }
        }
        super.keyPressed(character,keyCode);
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

    private void refreshPosition() {
        Vec2f igtPos, rtaPos;
        if (posTypesIGT.containsKey(currentPosType)) {
            igtPos = posTypesIGT.get(currentPosType);
        } else {
            igtPos = currentPosType == PositionType.DEFAULT
                    ? new Vec2f(SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_X), SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_Y))
                    : SpeedRunOption.getOption(currentPosType == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_IGT_POSITION_FOR_F3 : SpeedRunOptions.TIMER_IGT_POSITION_FOR_PAUSE);
        }
        if (posTypesRTA.containsKey(currentPosType)) {
            rtaPos = posTypesRTA.get(currentPosType);
        } else {
            rtaPos = currentPosType == PositionType.DEFAULT
                    ? new Vec2f(SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_X), SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_Y))
                    : SpeedRunOption.getOption(currentPosType == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_RTA_POSITION_FOR_F3 : SpeedRunOptions.TIMER_RTA_POSITION_FOR_PAUSE);
        }
        drawer.setIGT_XPos(igtPos.x);
        drawer.setIGT_YPos(igtPos.y);
        drawer.setRTA_XPos(rtaPos.x);
        drawer.setRTA_YPos(rtaPos.y);
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
                        return (new TranslatableText("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append((Math.round(drawer.getIGTScale() * 100)) + "%").asFormattedString());
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
                        return (new TranslatableText("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append((Math.round(drawer.getRTAScale() * 100)) + "%").asFormattedString());
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

    public void initPositionButtons() {
        ConsumerButtonWidget posTypeButton = addButton(new ConsumerButtonWidget(width / 2 - 80, height / 2 + 6, 160, 20, new TranslatableText("speedrunigt.option.timer_position.split_position_type").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.split_position_type."+currentPosType.name().toLowerCase(Locale.ROOT))).asFormattedString(), (button) -> {
            int order = (currentPosType.ordinal() + 1) % PositionType.values().length;
            currentPosType = PositionType.values()[order];
            changed = true;
            refreshPosition();
            button.message = (new TranslatableText("speedrunigt.option.timer_position.split_position_type").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.split_position_type."+currentPosType.name().toLowerCase(Locale.ROOT))).asFormattedString());
        }));
        posTypeButton.active = splitPosition;

        posOptions.add(
                addButton(new ConsumerButtonWidget(width / 2 - 80, height / 2 - 16, 160, 20, new TranslatableText("speedrunigt.option.timer_position.split_position").append(" : ").append(splitPosition ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString(), (button) -> {
                    splitPosition = !splitPosition;
                    changed = true;
                    posTypeButton.active = splitPosition;
                    button.message = (new TranslatableText("speedrunigt.option.timer_position.split_position").append(" : ").append(splitPosition ? ScreenTexts.ON : ScreenTexts.OFF).asFormattedString());
                    if (!splitPosition) {
                        currentPosType = PositionType.DEFAULT;
                        refreshPosition();
                        posTypeButton.message = (new TranslatableText("speedrunigt.option.timer_position.split_position_type").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.split_position_type."+currentPosType.name().toLowerCase(Locale.ROOT))).asFormattedString());
                    }
                }))
        );

        posOptions.add(posTypeButton);
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
