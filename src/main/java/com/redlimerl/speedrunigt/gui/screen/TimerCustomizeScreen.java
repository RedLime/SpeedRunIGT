package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.mixins.access.FontManagerAccessor;
import com.redlimerl.speedrunigt.mixins.access.MinecraftClientAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecimals;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.version.ColorMixer;
import com.redlimerl.speedrunigt.version.CustomSliderWidget;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class TimerCustomizeScreen extends Screen {

    private final TimerDrawer drawer = new TimerDrawer(false);
    private final Screen parent;

    private boolean changed = false;
    private boolean hide = false;
    private final ArrayList<AbstractButtonWidget> normalOptions = new ArrayList<>();
    private final ArrayList<AbstractButtonWidget> igtOptions = new ArrayList<>();
    private final ArrayList<AbstractButtonWidget> rtaOptions = new ArrayList<>();
    private final ArrayList<AbstractButtonWidget> fontOptions = new ArrayList<>();
    private final ArrayList<AbstractButtonWidget> backgroundOptions = new ArrayList<>();
    private ButtonWidget normalButton;
    private ButtonWidget igtButton;
    private ButtonWidget rtaButton;
    private ButtonWidget fontButton;
    private ButtonWidget backgroundButton;
    private ButtonWidget saveButton;
    private ButtonWidget fontConfigButton;

    private int fontPage = 0;
    private final ArrayList<Identifier> availableFonts = new ArrayList<>();
    private final ArrayList<ButtonWidget> fontSelectButtons = new ArrayList<>();

    public TimerCustomizeScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.option.timer_position"));
        this.parent = parent;
    }

    private int currentTab = 0;
    private void openTab(int tab) {
        currentTab = tab;
        this.normalButton.active = tab != 0;
        this.igtButton.active = tab != 1;
        this.rtaButton.active = tab != 2;
        this.fontButton.active = tab != 3;
        this.backgroundButton.active = tab != 4;

        if (hide) return;
        for (AbstractButtonWidget normalOption : normalOptions) {
            normalOption.visible = tab == 0;
        }
        for (AbstractButtonWidget igtOption : igtOptions) {
            igtOption.visible = tab == 1;
        }
        for (AbstractButtonWidget rtaOption : rtaOptions) {
            rtaOption.visible = tab == 2;
        }
        for (AbstractButtonWidget fontOption : fontOptions) {
            fontOption.visible = tab == 3;
        }
        for (AbstractButtonWidget backgroundOption : backgroundOptions) {
            backgroundOption.visible = tab == 4;
        }

        fontConfigButton.visible = tab == 3 && Objects.equals(drawer.getTimerFont().getNamespace(), SpeedRunIGT.MOD_ID);
    }

    @Override
    protected void init() {
        normalOptions.clear();
        igtOptions.clear();
        rtaOptions.clear();
        fontOptions.clear();
        availableFonts.clear();
        fontSelectButtons.clear();
        backgroundOptions.clear();

        if (minecraft != null) {
            FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftClientAccessor) minecraft).getFontManager();
            if (!fontManager.getTextRenderers().containsKey(drawer.getTimerFont())) {
                availableFonts.add(drawer.getTimerFont());
            }

            availableFonts.addAll(fontManager.getTextRenderers().keySet());
        }

        initNormal();
        initIGTButtons();
        initRTAButtons();
        initFontButtons();
        initBackgroundButtons();

        this.normalButton = addButton(new ButtonWidget(width / 2 - 149, height / 2 - 48, 58, 20, new TranslatableText("options.title").append("...").asFormattedString(), (ButtonWidget button) -> openTab(0)));

        this.igtButton = addButton(new ButtonWidget(width / 2 - 89, height / 2 - 48, 58, 20, new LiteralText("IGT...").asFormattedString(), (ButtonWidget button) -> openTab(1)));

        this.rtaButton = addButton(new ButtonWidget(width / 2 - 29, height / 2 - 48, 58, 20, new LiteralText("RTA...").asFormattedString(), (ButtonWidget button) -> openTab(2)));

        this.fontButton = addButton(new ButtonWidget(width / 2 + 31, height / 2 - 48, 58, 20, new TranslatableText("speedrunigt.title.font").asFormattedString(), (ButtonWidget button) -> {
            openTab(3);
            openFontPage();
        }));

        this.backgroundButton = addButton(new ButtonWidget(width / 2 + 91, height / 2 - 48, 58, 20, new TranslatableText("speedrunigt.title.background").asFormattedString(), (ButtonWidget button) -> openTab(4)));

        addButton(new ButtonWidget(width / 2 - 89, height / 2 + 62, 58, 20, new TranslatableText("speedrunigt.option.hide").asFormattedString(), (ButtonWidget button) -> {
            hide = !hide;
            for (AbstractButtonWidget normalOption : normalOptions) {
                normalOption.visible = !hide && currentTab == 0;
            }
            for (AbstractButtonWidget igtOption : igtOptions) {
                igtOption.visible = !hide && currentTab == 1;
            }
            for (AbstractButtonWidget rtaOption : rtaOptions) {
                rtaOption.visible = !hide && currentTab == 2;
            }
            for (AbstractButtonWidget fontOption : fontOptions) {
                fontOption.visible = !hide && currentTab == 3;
            }
            for (AbstractButtonWidget backgroundOption : backgroundOptions) {
                backgroundOption.visible = !hide && currentTab == 4;
            }
            openTab(currentTab);
            button.setMessage(new TranslatableText("speedrunigt.option." + (!hide ? "hide" : "show")).asFormattedString());
        }));

        this.saveButton = addButton(new ButtonWidget(width / 2 - 29, height / 2 + 62, 58, 20, new TranslatableText("selectWorld.edit.save").asFormattedString(), (ButtonWidget button) -> {
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

            SpeedRunOption.setOption(SpeedRunOptions.TIMER_TEXT_FONT, drawer.getTimerFont());
            SpeedRunIGT.TIMER_DRAWER.setTimerFont(drawer.getTimerFont());

            SpeedRunOption.setOption(SpeedRunOptions.BACKGROUND_OPACITY, drawer.getBGOpacity());
            SpeedRunIGT.TIMER_DRAWER.setBGOpacity(drawer.getBGOpacity());
            SpeedRunOption.setOption(SpeedRunOptions.RTA_BACKGROUND_PADDING, drawer.getRTAPadding());
            SpeedRunIGT.TIMER_DRAWER.setRTAPadding(drawer.getRTAPadding());
            SpeedRunOption.setOption(SpeedRunOptions.IGT_BACKGROUND_PADDING, drawer.getIGTPadding());
            SpeedRunIGT.TIMER_DRAWER.setIGTPadding(drawer.getIGTPadding());

            changed = false;
        }));

        addButton(new ButtonWidget(width / 2 + 31, height / 2 + 62, 58, 20, ScreenTexts.CANCEL, (ButtonWidget button) -> onClose()));

        fontConfigButton.visible = false;

        openTab(0);
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
                drawer.setIGT_XPos(MathHelper.clamp(drawer.getIGT_XPos() + moveX * drawer.getIGTScale() / minecraft.getWindow().getScaledWidth(), 0, 1));
                drawer.setIGT_YPos(MathHelper.clamp(drawer.getIGT_YPos() + moveY * drawer.getIGTScale() / minecraft.getWindow().getScaledHeight(), 0, 1));
                changed = true;
            }
            if (!rtaButton.active) {
                drawer.setRTA_XPos(MathHelper.clamp(drawer.getRTA_XPos() + moveX * drawer.getRTAScale() / minecraft.getWindow().getScaledWidth(), 0, 1));
                drawer.setRTA_YPos(MathHelper.clamp(drawer.getRTA_YPos() + moveY * drawer.getRTAScale() / minecraft.getWindow().getScaledHeight(), 0, 1));
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

        drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 15, 16777215);

        if (!hide) {
            if (!igtButton.active || !rtaButton.active) {
                if (drawer.isLocked()) {
                    drawCenteredString(this.font,
                            new TranslatableText("speedrunigt.option.timer_position.description.lock").asFormattedString(), this.width / 2, this.height / 2 - 80, 16777215);
                } else {
                    drawCenteredString(this.font,
                            new TranslatableText("speedrunigt.option.timer_position.description").asFormattedString(), this.width / 2, this.height / 2 - 80, 16777215);
                    drawCenteredString(this.font,
                            new TranslatableText("speedrunigt.option.timer_position.description.move").asFormattedString(), this.width / 2, this.height / 2 - 69, 16777215);
                }
            }


            if (!fontButton.active && minecraft != null) {
                int c = fontPage * 3;
                FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftClientAccessor) minecraft).getFontManager();
                for (int i = 0; i < fontSelectButtons.size(); i++) {
                    if (c + i < availableFonts.size()) {
                        Identifier fontIdentifier = availableFonts.get(c + i);
                        LiteralText text = new LiteralText(fontIdentifier.getPath());
                        TextRenderer targetFont = this.font;

                        if (minecraft != null && fontManager.getTextRenderers().containsKey(fontIdentifier)) {
                            targetFont = fontManager.getTextRenderers().get(fontIdentifier);
                        } else {
                            text.append(new LiteralText(" (Unavailable)")).formatted(Formatting.RED);
                        }

                        if (fontIdentifier.toString().equals(drawer.getTimerFont().toString())) {
                            text.append(" [Selected]").formatted(Formatting.ITALIC);
                        }
                        drawCenteredString(targetFont, text.asFormattedString(), this.width / 2 - 30,
                                this.height / 2 - 11 + (i * 22), 16777215);
                    }
                }
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
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 - 16, 120, 20, ColorMixer.getRed(drawer.getIGTColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_red", "IGT").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getIGTColor()))).asFormattedString());
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
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 + 6, 120, 20, ColorMixer.getGreen(drawer.getIGTColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_green", "IGT").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getIGTColor()))).asFormattedString());
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
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 + 28, 120, 20, ColorMixer.getBlue(drawer.getIGTColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_blue", "IGT").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getIGTColor()))).asFormattedString());
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
                addButton(new ButtonWidget(width / 2 + 6, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_decorate", "IGT").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))).asFormattedString(), (ButtonWidget button) -> {
                    int order = drawer.getIGTDecoration().ordinal();
                    drawer.setIGTDecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.text_decorate", "IGT").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))).asFormattedString());
                }))
        );
    }

    public void initRTAButtons() {
        rtaOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 - 16, 120, 20, ColorMixer.getRed(drawer.getRTAColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_red", "RTA").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getRTAColor()))).asFormattedString());
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
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 + 6, 120, 20, ColorMixer.getGreen(drawer.getRTAColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_green", "RTA").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getRTAColor()))).asFormattedString());
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
                addButton(new CustomSliderWidget(width / 2 - 127, height / 2 + 28, 120, 20, ColorMixer.getBlue(drawer.getRTAColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.color_blue", "RTA").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getRTAColor()))).asFormattedString());
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
                addButton(new ButtonWidget(width / 2 + 6, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_decorate", "RTA").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))).asFormattedString(), (ButtonWidget button) -> {
                    int order = drawer.getRTADecoration().ordinal();
                    drawer.setRTADecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.text_decorate", "RTA").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))).asFormattedString());
                }))
        );
    }

    public void initFontButtons() {
        ButtonWidget prevButton = addButton(new ButtonWidget(width / 2 - 180, height / 2 + 6, 20, 20, "<", (ButtonWidget button) -> {
            fontPage--;
            openFontPage();
        }));

        ButtonWidget nextButton = addButton(new ButtonWidget(width / 2 + 180, height / 2 + 6, 20, 20, ">", (ButtonWidget button) -> {
            fontPage++;
            openFontPage();
        }));
        fontOptions.add(prevButton);
        fontOptions.add(nextButton);

        fontSelectButtons.add(
                addButton(new ButtonWidget(width / 2 + 35, height / 2 - 16, 50, 20, new TranslatableText("speedrunigt.option.select").asFormattedString(), (ButtonWidget button) -> {
                    int c = (fontPage * 3);
                    if (availableFonts.size() > c) {
                        for (ButtonWidget fontSelectButton : fontSelectButtons) fontSelectButton.active = true;
                        drawer.setTimerFont(availableFonts.get(c));
                        button.active = false;
                        openFontPage();
                        changed = true;
                    }
                }))
        );
        fontSelectButtons.add(
                addButton(new ButtonWidget(width / 2 + 35, height / 2 + 6, 50, 20, new TranslatableText("speedrunigt.option.select").asFormattedString(), (ButtonWidget button) -> {
                    int c = (fontPage * 3) + 1;
                    if (availableFonts.size() > c) {
                        for (ButtonWidget fontSelectButton : fontSelectButtons) fontSelectButton.active = true;
                        drawer.setTimerFont(availableFonts.get(c));
                        button.active = false;
                        openFontPage();
                        changed = true;
                    }
                }))
        );
        fontSelectButtons.add(
                addButton(new ButtonWidget(width / 2 + 35, height / 2 + 28, 50, 20, new TranslatableText("speedrunigt.option.select").asFormattedString(), (ButtonWidget button) -> {
                    int c = (fontPage * 3) + 2;
                    if (availableFonts.size() > c) {
                        for (ButtonWidget fontSelectButton : fontSelectButtons) fontSelectButton.active = true;
                        drawer.setTimerFont(availableFonts.get(c));
                        button.active = false;
                        openFontPage();
                        changed = true;
                    }
                }))
        );
        for (AbstractButtonWidget fontOption : fontOptions) {
            fontOption.visible = false;
        }
        for (ButtonWidget fontSelectButton : fontSelectButtons) {
            fontSelectButton.visible = false;
        }
        fontOptions.addAll(fontSelectButtons);


        fontConfigButton = addButton(new ButtonWidget(width / 2 + 88, 0, 50, 20, new LiteralText("Config").asFormattedString(), (ButtonWidget button) -> {
            if (minecraft != null) minecraft.openScreen(new FontConfigScreen(this, drawer.getTimerFont()));
        } ));
        fontOptions.add(addButton(new ButtonWidget(width / 2 - 154, height / 2 - 80, 150, 20, new TranslatableText("speedrunigt.option.timer_position.font.open_folder").asFormattedString(), (ButtonWidget button) -> Util.getOperatingSystem().open(SpeedRunIGT.FONT_PATH.toFile()))));
        fontOptions.add(addButton(new ButtonWidget(width / 2 + 4, height / 2 - 80, 150, 20, new TranslatableText("speedrunigt.option.timer_position.font.description").asFormattedString(), (ButtonWidget button) -> Util.getOperatingSystem().open("https://youtu.be/agBbiTQWj78"))));
        openFontPage();
    }

    public void openFontPage() {
        fontOptions.get(0).active = fontPage != 0;
        fontOptions.get(1).active = fontPage != Math.max((availableFonts.size() - 1) / 3, 0);

        int c = fontPage * 3;
        int available = 0;
        for (int i = 0; i < fontSelectButtons.size(); i++) {
            ButtonWidget button = fontSelectButtons.get(i);
            if (c + i < availableFonts.size()) {
                button.active = !availableFonts.get(c + i).toString().equals(drawer.getTimerFont().toString());
                if (!button.active && Objects.equals(drawer.getTimerFont().getNamespace(), SpeedRunIGT.MOD_ID)) available = button.y;
                button.visible = true;
            } else {
                button.visible = false;
            }
        }

        fontConfigButton.visible = currentTab == 3 && available != 0;
        fontConfigButton.y = available;
    }

    public void initBackgroundButtons() {
        backgroundOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 80, height / 2 - 16, 160, 20, drawer.getBGOpacity()) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.background_opacity").append(" : ").append((int) (drawer.getBGOpacity()*100) + "%").asFormattedString());
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setBGOpacity((float) this.value);
                        changed = true;
                    }
                })
        );

        backgroundOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 80, height / 2 + 6, 160, 20, (drawer.getRTAPadding()-1) / 24f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.background_padding", "RTA").append(" : ").append(String.valueOf(drawer.getRTAPadding())).asFormattedString());
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setRTAPadding((int) (this.value * 24) + 1);
                        changed = true;
                    }
                })
        );

        backgroundOptions.add(
                addButton(new CustomSliderWidget(width / 2 - 80, height / 2 + 28, 160, 20,(drawer.getIGTPadding()-1) / 24f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.background_padding", "IGT").append(" : ").append(String.valueOf(drawer.getIGTPadding())).asFormattedString());
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setIGTPadding((int) (this.value * 24) + 1);
                        changed = true;
                    }
                })
        );
    }
}
