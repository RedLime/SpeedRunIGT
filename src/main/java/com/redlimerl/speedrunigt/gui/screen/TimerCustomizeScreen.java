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
import net.minecraft.class_0_686;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TimerCustomizeScreen extends Screen {

    private final TimerDrawer drawer = new TimerDrawer(false);
    private final Screen parent;

    private PositionType currentPosType = PositionType.DEFAULT;
    private final HashMap<PositionType, Vec2f> posTypesRTA = new HashMap<>();
    private final HashMap<PositionType, Vec2f> posTypesIGT = new HashMap<>();

    private boolean changed = false;
    private boolean hide = false;
    private final ArrayList<ClickableWidget> tabButtons = new ArrayList<>();
    private final ArrayList<ClickableWidget> normalOptions = new ArrayList<>();
    private final ArrayList<ClickableWidget> igtOptions = new ArrayList<>();
    private final ArrayList<ClickableWidget> rtaOptions = new ArrayList<>();
    private final ArrayList<ClickableWidget> posOptions = new ArrayList<>();
    private final ArrayList<ClickableWidget> backgroundOptions = new ArrayList<>();
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
        this.normalButton.field_2078 = tab != 0;
        this.igtButton.field_2078 = tab != 1;
        this.rtaButton.field_2078 = tab != 2;
        this.posButton.field_2078 = tab != 5;
        this.backgroundButton.field_2078 = tab != 4;

        if (hide) return;
        for (ClickableWidget normalOption : normalOptions) {
            normalOption.field_2076 = tab == 0;
        }
        for (ClickableWidget igtOption : igtOptions) {
            igtOption.field_2076 = tab == 1;
        }
        for (ClickableWidget rtaOption : rtaOptions) {
            rtaOption.field_2076 = tab == 2;
        }
        for (ClickableWidget backgroundOption : backgroundOptions) {
            backgroundOption.field_2076 = tab == 4;
        }
        for (ClickableWidget posOption : posOptions) {
            posOption.field_2076 = tab == 5;
        }
    }

    @Override
    protected void method_0_2778(ClickableWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.method_0_2778(button);
    }

    public <T extends ClickableWidget> T addButton(T button) {
        field_2564.add(button);
        return button;
    }

    @Override
    public void method_2224() {
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

        this.normalButton = new ConsumerButtonWidget(field_2561 / 2 - 179, field_2559 / 2 - 48, 58, 20, new TranslatableTextContent("options.title").append("...").method_10865(), (button) -> openTab(0));
        field_2564.add(this.normalButton);
        this.tabButtons.add(this.normalButton);

        this.igtButton = new ConsumerButtonWidget(field_2561 / 2 - 119, field_2559 / 2 - 48, 58, 20, new LiteralTextContent("IGT...").method_10865(), (button) -> openTab(1));
        field_2564.add(this.igtButton);
        this.tabButtons.add(this.igtButton);

        this.rtaButton = new ConsumerButtonWidget(field_2561 / 2 - 59, field_2559 / 2 - 48, 58, 20, new LiteralTextContent("RTA...").method_10865(), (button) -> openTab(2));
        field_2564.add(this.rtaButton);
        this.tabButtons.add(this.rtaButton);

        this.posButton = addButton(new ConsumerButtonWidget(field_2561 / 2 + 1, field_2559 / 2 - 48, 58, 20, "Pos...", (ClickableWidget button) -> openTab(5)));
        field_2564.add(this.posButton);
        this.tabButtons.add(this.posButton);

        ClickableWidget fontButton = new ConsumerButtonWidget(field_2561 / 2 + 61, field_2559 / 2 - 48, 58, 20, new TranslatableTextContent("speedrunigt.title.font").method_10865(), button -> {});
        fontButton.field_2078 = false;
        field_2564.add(fontButton);
        this.tabButtons.add(fontButton);

        this.backgroundButton = new ConsumerButtonWidget(field_2561 / 2 + 121, field_2559 / 2 - 48, 58, 20, new TranslatableTextContent("speedrunigt.title.background").method_10865(), (button) -> openTab(4));
        field_2564.add(this.backgroundButton);
        this.tabButtons.add(this.backgroundButton);

        field_2564.add(new ConsumerButtonWidget(field_2561 / 2 - 89, field_2559 / 2 + 62, 58, 20, new TranslatableTextContent("speedrunigt.option.hide").method_10865(), (button) -> {
            hide = !hide;
            for (ClickableWidget normalOption : normalOptions) {
                normalOption.field_2076 = !hide && currentTab == 0;
            }
            for (ClickableWidget igtOption : igtOptions) {
                igtOption.field_2076 = !hide && currentTab == 1;
            }
            for (ClickableWidget rtaOption : rtaOptions) {
                rtaOption.field_2076 = !hide && currentTab == 2;
            }
            for (ClickableWidget posOption : posOptions) {
                posOption.field_2076 = !hide && currentTab == 5;
            }
            for (ClickableWidget backgroundOption : backgroundOptions) {
                backgroundOption.field_2076 = !hide && currentTab == 4;
            }
            for (ClickableWidget tabButton : tabButtons) {
                tabButton.field_2076 = !hide;
            }
            openTab(currentTab);
            button.field_2074 = new TranslatableTextContent("speedrunigt.option." + (!hide ? "hide" : "show")).method_10865();
        }));

        this.saveButton = new ConsumerButtonWidget(field_2561 / 2 - 29, field_2559 / 2 + 62, 58, 20, new TranslatableTextContent("speedrunigt.option.save").method_10865(), (button) -> {
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
        field_2564.add(this.saveButton);

        field_2564.add(new ConsumerButtonWidget(field_2561 / 2 + 31, field_2559 / 2 + 62, 58, 20, ScreenTexts.CANCEL, (button) -> onClose()));

        openTab(0);
    }

    @Override
    protected void method_0_2775(int mouseX, int mouseY, int button) {
        boolean isButtonClick = false;
        for (ClickableWidget widget : this.field_2564) {
            if (widget.method_1829(this.field_2563, mouseX, mouseY)) {
                isButtonClick = true;
            }
        }
        if (button == 0 && !drawer.isLocked()&&!isButtonClick) {
            class_0_686 window = new class_0_686(field_2563);
            if (!this.igtButton.field_2078) {
                drawer.setIGT_XPos((float) MathHelper.clamp(mouseX / window.method_0_2461(), 0, 1));
                drawer.setIGT_YPos((float) MathHelper.clamp(mouseY / window.method_0_2462(), 0, 1));
                posTypesIGT.put(currentPosType, new Vec2f(drawer.getIGT_XPos(), drawer.getIGT_YPos()));
                changed = true;
            }
            if (!this.rtaButton.field_2078) {
                drawer.setRTA_XPos((float) MathHelper.clamp(mouseX / window.method_0_2461(), 0, 1));
                drawer.setRTA_YPos((float) MathHelper.clamp(mouseY / window.method_0_2462(), 0, 1));
                posTypesRTA.put(currentPosType, new Vec2f(drawer.getRTA_XPos(), drawer.getRTA_YPos()));
                changed = true;
            }
        }
        super.method_0_2775(mouseX,mouseY,button);
    }

    @Override
    protected void method_0_2773(char character, int keyCode) {
        if (method_2238() && keyCode >= 200 && keyCode <= 208 && field_2563 != null && !drawer.isLocked()) {
            int moveX = keyCode == 205 ? 1 : keyCode == 203 ? -1 : 0;
            int moveY = keyCode == 200 ? -1 : keyCode == 208 ? 1 : 0;
            class_0_686 window = new class_0_686(field_2563);
            if (!igtButton.field_2078) {
                drawer.setIGT_XPos(MathHelper.clamp(drawer.getIGT_XPos() + moveX * drawer.getIGTScale() / (float) window.method_0_2461(), 0, 1));
                drawer.setIGT_YPos(MathHelper.clamp(drawer.getIGT_YPos() + moveY * drawer.getIGTScale() / (float) window.method_0_2462(), 0, 1));
                posTypesIGT.put(currentPosType, new Vec2f(drawer.getIGT_XPos(), drawer.getIGT_YPos()));
                changed = true;
            }
            if (!rtaButton.field_2078) {
                drawer.setRTA_XPos(MathHelper.clamp(drawer.getRTA_XPos() + moveX * drawer.getRTAScale() / (float) window.method_0_2461(), 0, 1));
                drawer.setRTA_YPos(MathHelper.clamp(drawer.getRTA_YPos() + moveY * drawer.getRTAScale() / (float) window.method_0_2462(), 0, 1));
                posTypesRTA.put(currentPosType, new Vec2f(drawer.getRTA_XPos(), drawer.getRTA_YPos()));
                changed = true;
            }
        }
        super.method_0_2773(character,keyCode);
    }

    @Override
    public void method_2214(int mouseX, int mouseY, float delta) {
        saveButton.field_2078 = changed;

        this.method_2240();

        drawer.draw();

        method_1789(this.field_2554, new TranslatableTextContent("speedrunigt.option.timer_position").method_10865(), this.field_2561 / 2, 15, 16777215);

        if (!hide) {
            if (!igtButton.field_2078 || !rtaButton.field_2078) {
                if (drawer.isLocked()) {
                    method_1789(this.field_2554,
                            new TranslatableTextContent("speedrunigt.option.timer_position.description.lock").method_10865(), this.field_2561 / 2, this.field_2559 / 2 - 80, 16777215);
                } else {
                    method_1789(this.field_2554,
                            new TranslatableTextContent("speedrunigt.option.timer_position.description").method_10865(), this.field_2561 / 2, this.field_2559 / 2 - 80, 16777215);
                    method_1789(this.field_2554,
                            new TranslatableTextContent("speedrunigt.option.timer_position.description.move").method_10865(), this.field_2561 / 2, this.field_2559 / 2 - 69, 16777215);
                }
            }
        }
        super.method_2214(mouseX, mouseY, delta);
    }

    public void onClose() {
        assert this.field_2563 != null;
        this.field_2563.setScreen(parent);
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
                addButton(new ConsumerButtonWidget(field_2561 / 2 - 80, field_2559 / 2 - 16, 160, 20, new TranslatableTextContent("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(), (button) -> {
                    drawer.setSimplyTimer(!drawer.isSimplyTimer());
                    changed = true;
                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                }))
        );

        normalOptions.add(
                addButton(new ConsumerButtonWidget(field_2561 / 2 - 80, field_2559 / 2 + 6, 160, 20, new TranslatableTextContent("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(), (button) -> {
                    drawer.setLocked(!drawer.isLocked());
                    changed = true;
                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                }))
        );

        normalOptions.add(
                addButton(new ConsumerButtonWidget(field_2561 / 2 - 80, field_2559 / 2 + 28, 160, 20, new TranslatableTextContent("speedrunigt.option.timer_position.show_decimals").append(" : ").append(new TranslatableTextContent("speedrunigt.option.timer_position.show_decimals.context", drawer.getTimerDecimals().getNumber())).method_10865(), (button) -> {
                    int order = drawer.getTimerDecimals().ordinal();
                    drawer.setTimerDecimals(TimerDecimals.values()[(++order) % TimerDecimals.values().length]);
                    changed = true;
                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.timer_position.show_decimals").append(" : ").append(new TranslatableTextContent("speedrunigt.option.timer_position.show_decimals.context", drawer.getTimerDecimals().getNumber())).method_10865());
                }))
        );
    }

    public void initIGTButtons() {
        igtOptions.add(
                addButton(new CustomSliderWidget(field_2561 / 2 - 127, field_2559 / 2 - 16, 120, 20, ColorMixer.getRed(drawer.getIGTColor()) / 255.0f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableTextContent("speedrunigt.option.timer_position.color_red", "IGT").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getIGTColor()))).method_10865());
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
                addButton(new CustomSliderWidget(field_2561 / 2 - 127, field_2559 / 2 + 6, 120, 20, ColorMixer.getGreen(drawer.getIGTColor()) / 255.0f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableTextContent("speedrunigt.option.timer_position.color_green", "IGT").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getIGTColor()))).method_10865());
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
                addButton(new CustomSliderWidget(field_2561 / 2 - 127, field_2559 / 2 + 28, 120, 20, ColorMixer.getBlue(drawer.getIGTColor()) / 255.0f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableTextContent("speedrunigt.option.timer_position.color_blue", "IGT").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getIGTColor()))).method_10865());
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
                addButton(new CustomSliderWidget(field_2561 / 2 + 6, field_2559 / 2 - 16, 120, 20, drawer.getIGTScale() / 3f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableTextContent("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append((Math.round(drawer.getIGTScale() * 100)) + "%").method_10865());
                    }

                    @Override
                    public void applyValue(float value) {
                        drawer.setIGTScale(Math.round(value * 3f * 20f)/20f);
                        changed = true;
                    }
                }))
        );

        igtOptions.add(
                addButton(new ConsumerButtonWidget(field_2561 / 2 + 6, field_2559 / 2 + 6, 120, 20, new TranslatableTextContent("speedrunigt.option.timer_position.text_decorate", "IGT").append(" : ").append(new TranslatableTextContent("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))).method_10865(), (button) -> {
                    int order = drawer.getIGTDecoration().ordinal();
                    drawer.setIGTDecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    changed = true;
                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.timer_position.text_decorate", "IGT").append(" : ").append(new TranslatableTextContent("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))).method_10865());
                }))
        );
    }

    public void initRTAButtons() {
        rtaOptions.add(
                addButton(new CustomSliderWidget(field_2561 / 2 - 127, field_2559 / 2 - 16, 120, 20, ColorMixer.getRed(drawer.getRTAColor()) / 255.0f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableTextContent("speedrunigt.option.timer_position.color_red", "RTA").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getRTAColor()))).method_10865());
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
                addButton(new CustomSliderWidget(field_2561 / 2 - 127, field_2559 / 2 + 6, 120, 20, ColorMixer.getGreen(drawer.getRTAColor()) / 255.0f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableTextContent("speedrunigt.option.timer_position.color_green", "RTA").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getRTAColor()))).method_10865());
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
                addButton(new CustomSliderWidget(field_2561 / 2 - 127, field_2559 / 2 + 28, 120, 20, ColorMixer.getBlue(drawer.getRTAColor()) / 255.0f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableTextContent("speedrunigt.option.timer_position.color_blue", "RTA").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getRTAColor()))).method_10865());
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
                addButton(new CustomSliderWidget(field_2561 / 2 + 6, field_2559 / 2 - 16, 120, 20, drawer.getRTAScale() / 3f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableTextContent("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append((Math.round(drawer.getRTAScale() * 100)) + "%").method_10865());
                    }

                    @Override
                    public void applyValue(float value) {
                        drawer.setRTAScale(Math.round(value * 3f * 20f)/20f);
                        changed = true;
                    }
                }))
        );

        rtaOptions.add(
                addButton(new ConsumerButtonWidget(field_2561 / 2 + 6, field_2559 / 2 + 6, 120, 20, new TranslatableTextContent("speedrunigt.option.timer_position.text_decorate", "RTA").append(" : ").append(new TranslatableTextContent("speedrunigt.option.timer_position.text_decorate." + drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))).method_10865(), (button) -> {
                    int order = drawer.getRTADecoration().ordinal();
                    drawer.setRTADecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    changed = true;
                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.timer_position.text_decorate", "RTA").append(" : ").append(new TranslatableTextContent("speedrunigt.option.timer_position.text_decorate." + drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))).method_10865());
                }))
        );
    }

    public void initPositionButtons() {
        ConsumerButtonWidget posTypeButton = addButton(new ConsumerButtonWidget(field_2561 / 2 - 80, field_2559 / 2 + 6, 160, 20, new TranslatableTextContent("speedrunigt.option.timer_position.split_position_type").append(" : ").append(new TranslatableTextContent("speedrunigt.option.timer_position.split_position_type."+currentPosType.name().toLowerCase(Locale.ROOT))).method_10865(), (button) -> {
            int order = (currentPosType.ordinal() + 1) % PositionType.values().length;
            currentPosType = PositionType.values()[order];
            changed = true;
            refreshPosition();
            button.field_2074 = (new TranslatableTextContent("speedrunigt.option.timer_position.split_position_type").append(" : ").append(new TranslatableTextContent("speedrunigt.option.timer_position.split_position_type."+currentPosType.name().toLowerCase(Locale.ROOT))).method_10865());
        }));
        posTypeButton.field_2078 = splitPosition;

        posOptions.add(
                addButton(new ConsumerButtonWidget(field_2561 / 2 - 80, field_2559 / 2 - 16, 160, 20, new TranslatableTextContent("speedrunigt.option.timer_position.split_position").append(" : ").append(splitPosition ? ScreenTexts.ON : ScreenTexts.OFF).method_10865(), (button) -> {
                    splitPosition = !splitPosition;
                    changed = true;
                    posTypeButton.field_2078 = splitPosition;
                    button.field_2074 = (new TranslatableTextContent("speedrunigt.option.timer_position.split_position").append(" : ").append(splitPosition ? ScreenTexts.ON : ScreenTexts.OFF).method_10865());
                    if (!splitPosition) {
                        currentPosType = PositionType.DEFAULT;
                        refreshPosition();
                        posTypeButton.field_2074 = (new TranslatableTextContent("speedrunigt.option.timer_position.split_position_type").append(" : ").append(new TranslatableTextContent("speedrunigt.option.timer_position.split_position_type."+currentPosType.name().toLowerCase(Locale.ROOT))).method_10865());
                    }
                }))
        );

        posOptions.add(posTypeButton);
    }

    public void initBackgroundButtons() {
        backgroundOptions.add(
                addButton(new CustomSliderWidget(field_2561 / 2 - 80, field_2559 / 2 - 16, 160, 20, drawer.getBGOpacity(), new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableTextContent("speedrunigt.option.timer_position.background_opacity").append(" : ").append((int) (drawer.getBGOpacity()*100) + "%").method_10865());
                    }

                    @Override
                    public void applyValue(float value) {
                        drawer.setBGOpacity(value);
                        changed = true;
                    }
                }))
        );

        backgroundOptions.add(
                addButton(new CustomSliderWidget(field_2561 / 2 - 80, field_2559 / 2 + 6, 160, 20, (drawer.getRTAPadding()-1) / 24f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableTextContent("speedrunigt.option.timer_position.background_padding", "RTA").append(" : ").append(String.valueOf(drawer.getRTAPadding())).method_10865());
                    }

                    @Override
                    public void applyValue(float value) {
                        drawer.setRTAPadding((int) (value * 24) + 1);
                        changed = true;
                    }
                }))
        );

        backgroundOptions.add(
                addButton(new CustomSliderWidget(field_2561 / 2 - 80, field_2559 / 2 + 28, 160, 20,(drawer.getIGTPadding()-1) / 24f, new CustomSliderWidget.SliderWorker() {
                    @Override
                    public String updateMessage() {
                        return (new TranslatableTextContent("speedrunigt.option.timer_position.background_padding", "IGT").append(" : ").append(String.valueOf(drawer.getIGTPadding())).method_10865());
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
