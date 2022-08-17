package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.mixins.access.FontManagerAccessor;
import com.redlimerl.speedrunigt.mixins.access.MinecraftClientAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecimals;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.timer.TimerDrawer.PositionType;
import net.minecraft.client.gui.hud.BackgroundHelper.ColorMixer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.util.*;

public class TimerCustomizeScreen extends Screen {

    private final TimerDrawer drawer = new TimerDrawer(false);
    private final Screen parent;

    private PositionType currentPosType = PositionType.DEFAULT;
    private final HashMap<PositionType, Vec2f> posTypesRTA = new HashMap<>();
    private final HashMap<PositionType, Vec2f> posTypesIGT = new HashMap<>();

    private boolean changed = false;
    private boolean hide = false;
    private final ArrayList<AbstractButtonWidget> tabButtons = new ArrayList<>();
    private final ArrayList<AbstractButtonWidget> normalOptions = new ArrayList<>();
    private final ArrayList<AbstractButtonWidget> igtOptions = new ArrayList<>();
    private final ArrayList<AbstractButtonWidget> rtaOptions = new ArrayList<>();
    private final ArrayList<AbstractButtonWidget> posOptions = new ArrayList<>();
    private final ArrayList<AbstractButtonWidget> fontOptions = new ArrayList<>();
    private final ArrayList<AbstractButtonWidget> backgroundOptions = new ArrayList<>();
    private ButtonWidget normalButton;
    private ButtonWidget igtButton;
    private ButtonWidget rtaButton;
    private ButtonWidget posButton;
    private ButtonWidget fontButton;
    private ButtonWidget backgroundButton;
    private ButtonWidget saveButton;
    private ButtonWidget fontConfigButton;

    private int fontPage = 0;
    private final ArrayList<Identifier> availableFonts = new ArrayList<>();
    private final ArrayList<ButtonWidget> fontSelectButtons = new ArrayList<>();

    private boolean splitPosition = SpeedRunOption.getOption(SpeedRunOptions.ENABLE_TIMER_SPLIT_POS);

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
        this.posButton.active = tab != 5;
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
        for (AbstractButtonWidget posOption : posOptions) {
            posOption.visible = tab == 5;
        }

        fontConfigButton.visible = tab == 3 && Objects.equals(drawer.getTimerFont().getNamespace(), SpeedRunIGT.MOD_ID);
    }

    @Override
    protected void init() {
        normalOptions.clear();
        igtOptions.clear();
        rtaOptions.clear();
        posOptions.clear();
        fontOptions.clear();
        availableFonts.clear();
        fontSelectButtons.clear();
        backgroundOptions.clear();

        if (client != null) {
            FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftClientAccessor) client).getFontManager();
            if (!fontManager.getFontStorages().containsKey(drawer.getTimerFont())) {
                availableFonts.add(drawer.getTimerFont());
            }

            availableFonts.addAll(fontManager.getFontStorages().keySet());
        }

        initNormal();
        initIGTButtons();
        initRTAButtons();
        initPositionButtons();
        initFontButtons();
        initBackgroundButtons();

        this.normalButton = addButton(new ButtonWidget(width / 2 - 179, height / 2 - 48, 58, 20, new TranslatableText("options.title").append("..."), (ButtonWidget button) -> openTab(0)));
        this.tabButtons.add(this.normalButton);

        this.igtButton = addButton(new ButtonWidget(width / 2 - 119, height / 2 - 48, 58, 20, new LiteralText("IGT..."), (ButtonWidget button) -> openTab(1)));
        this.tabButtons.add(this.igtButton);

        this.rtaButton = addButton(new ButtonWidget(width / 2 - 59, height / 2 - 48, 58, 20, new LiteralText("RTA..."), (ButtonWidget button) -> openTab(2)));
        this.tabButtons.add(this.rtaButton);

        this.posButton = addButton(new ButtonWidget(width / 2 + 1, height / 2 - 48, 58, 20, new LiteralText("Pos..."), (ButtonWidget button) -> openTab(5)));
        this.tabButtons.add(this.posButton);

        this.fontButton = addButton(new ButtonWidget(width / 2 + 61, height / 2 - 48, 58, 20, new TranslatableText("speedrunigt.title.font"), (ButtonWidget button) -> {
            openTab(3);
            openFontPage();
        }));
        this.tabButtons.add(this.fontButton);

        this.backgroundButton = addButton(new ButtonWidget(width / 2 + 121, height / 2 - 48, 58, 20, new TranslatableText("speedrunigt.title.background"), (ButtonWidget button) -> openTab(4)));
        this.tabButtons.add(this.backgroundButton);


        addButton(new ButtonWidget(width / 2 - 89, height / 2 + 62, 58, 20, new TranslatableText("speedrunigt.option.hide"), (ButtonWidget button) -> {
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
            for (AbstractButtonWidget posOption : posOptions) {
                posOption.visible = !hide && currentTab == 5;
            }
            for (AbstractButtonWidget fontOption : fontOptions) {
                fontOption.visible = !hide && currentTab == 3;
            }
            for (AbstractButtonWidget backgroundOption : backgroundOptions) {
                backgroundOption.visible = !hide && currentTab == 4;
            }
            for (AbstractButtonWidget tabButton : tabButtons) {
                tabButton.visible = !hide;
            }
            openTab(currentTab);
            button.setMessage(new TranslatableText("speedrunigt.option." + (!hide ? "hide" : "show")));
        }));

        this.saveButton = addButton(new ButtonWidget(width / 2 - 29, height / 2 + 62, 58, 20, new TranslatableText("selectWorld.edit.save"), (ButtonWidget button) -> {
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

            SpeedRunOption.setOption(SpeedRunOptions.TIMER_TEXT_FONT, drawer.getTimerFont());
            SpeedRunIGTClient.TIMER_DRAWER.setTimerFont(drawer.getTimerFont());

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

            SpeedRunOption.setOption(SpeedRunOptions.ENABLE_TIMER_SPLIT_POS, splitPosition);

            changed = false;
        }));

        addButton(new ButtonWidget(width / 2 + 31, height / 2 + 62, 58, 20, ScreenTexts.CANCEL, (ButtonWidget button) -> {
            if (client != null) client.openScreen(parent);
        }));

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
                posTypesIGT.put(currentPosType, new Vec2f(drawer.getIGT_XPos(), drawer.getIGT_YPos()));
                changed = true;
            }
            if (!this.rtaButton.active) {
                drawer.setRTA_XPos(MathHelper.clamp((float) (mouseX / width), 0, 1));
                drawer.setRTA_YPos(MathHelper.clamp((float) (mouseY / height), 0, 1));
                posTypesRTA.put(currentPosType, new Vec2f(drawer.getRTA_XPos(), drawer.getRTA_YPos()));
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
                posTypesIGT.put(currentPosType, new Vec2f(drawer.getIGT_XPos(), drawer.getIGT_YPos()));
                changed = true;
            }
            if (!rtaButton.active) {
                drawer.setRTA_XPos(MathHelper.clamp(drawer.getRTA_XPos() + moveX * drawer.getRTAScale() / client.getWindow().getScaledWidth(), 0, 1));
                drawer.setRTA_YPos(MathHelper.clamp(drawer.getRTA_YPos() + moveY * drawer.getRTAScale() / client.getWindow().getScaledHeight(), 0, 1));
                posTypesRTA.put(currentPosType, new Vec2f(drawer.getRTA_XPos(), drawer.getRTA_YPos()));
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

        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);

        if (!hide) {
            if (!igtButton.active || !rtaButton.active) {
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

            if (!fontButton.active && client != null) {
                int c = fontPage * 3;
                FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftClientAccessor) client).getFontManager();
                for (int i = 0; i < fontSelectButtons.size(); i++) {
                    if (c + i < availableFonts.size()) {
                        Identifier fontIdentifier = availableFonts.get(c + i);
                        LiteralText text = new LiteralText(fontIdentifier.getPath());

                        if (client != null && fontManager.getFontStorages().containsKey(fontIdentifier) && !SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE)) {
                            text.setStyle(text.getStyle().withFont(fontIdentifier));
                        } else {
                            text.append(new LiteralText(" (Unavailable)")).formatted(Formatting.RED);
                        }

                        if (fontIdentifier.toString().equals(drawer.getTimerFont().toString())) {
                            text.append(" [Selected]").formatted(Formatting.ITALIC);
                        }
                        drawCenteredText(matrices, this.textRenderer, text, this.width / 2 - 30,
                                this.height / 2 - 11 + (i * 22), 16777215);
                    }
                }
            }
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        assert client != null;
        client.openScreen(parent);
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
                addButton(new ButtonWidget(width / 2 - 80, height / 2 - 16, 160, 20, new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
                    drawer.setSimplyTimer(!drawer.isSimplyTimer());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF));
                }))
        );

        normalOptions.add(
                addButton(new ButtonWidget(width / 2 - 80, height / 2 + 6, 160, 20, new TranslatableText("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
                    drawer.setLocked(!drawer.isLocked());
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF));
                }))
        );

        normalOptions.add(
                addButton(new ButtonWidget(width / 2 - 80, height / 2 + 28, 160, 20, new TranslatableText("speedrunigt.option.timer_position.show_decimals").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.show_decimals.context", drawer.getTimerDecimals().getNumber())), (ButtonWidget button) -> {
                    int order = drawer.getTimerDecimals().ordinal();
                    drawer.setTimerDecimals(TimerDecimals.values()[(++order) % TimerDecimals.values().length]);
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.show_decimals").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.show_decimals.context", drawer.getTimerDecimals().getNumber())));
                }, (button, matrices, mouseX, mouseY) -> renderTooltip(matrices, new TranslatableText("speedrunigt.option.timer_position.show_decimals.description"), mouseX, mouseY)))
        );
    }

    public void initIGTButtons() {
        igtOptions.add(
                addButton(new SliderWidget(width / 2 - 127, height / 2 - 16, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_red", "IGT").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getIGTColor()))), ColorMixer.getRed(drawer.getIGTColor()) / 255.0f) {
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
                addButton(new SliderWidget(width / 2 - 127, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_green", "IGT").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getIGTColor()))), ColorMixer.getGreen(drawer.getIGTColor()) / 255.0f) {
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
                addButton(new SliderWidget(width / 2 - 127, height / 2 + 28, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_blue", "IGT").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getIGTColor()))), ColorMixer.getBlue(drawer.getIGTColor()) / 255.0f) {
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
                addButton(new SliderWidget(width / 2 + 6, height / 2 - 16, 120, 20, new TranslatableText("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append(((int) (drawer.getIGTScale() * 100)) + "%"), drawer.getIGTScale() / 3f) {
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
                addButton(new ButtonWidget(width / 2 + 6, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_decorate", "IGT").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))), (ButtonWidget button) -> {
                    int order = drawer.getIGTDecoration().ordinal();
                    drawer.setIGTDecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.text_decorate", "IGT").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))));
                }))
        );
    }

    public void initRTAButtons() {
        rtaOptions.add(
                addButton(new SliderWidget(width / 2 - 127, height / 2 - 16, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_red", "RTA").append(" : ").append(String.valueOf(ColorMixer.getRed(drawer.getRTAColor()))), ColorMixer.getRed(drawer.getRTAColor()) / 255.0f) {
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
                addButton(new SliderWidget(width / 2 - 127, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_green", "RTA").append(" : ").append(String.valueOf(ColorMixer.getGreen(drawer.getRTAColor()))), ColorMixer.getGreen(drawer.getRTAColor()) / 255.0f) {
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
                addButton(new SliderWidget(width / 2 - 127, height / 2 + 28, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_blue", "RTA").append(" : ").append(String.valueOf(ColorMixer.getBlue(drawer.getRTAColor()))), ColorMixer.getBlue(drawer.getRTAColor()) / 255.0f) {
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
                addButton(new SliderWidget(width / 2 + 6, height / 2 - 16, 120, 20, new TranslatableText("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append(((int) (drawer.getRTAScale() * 100)) + "%"), drawer.getRTAScale() / 3f) {
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
                addButton(new ButtonWidget(width / 2 + 6, height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_decorate", "RTA").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))), (ButtonWidget button) -> {
                    int order = drawer.getRTADecoration().ordinal();
                    drawer.setRTADecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.text_decorate", "RTA").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))));
                }))
        );
    }

    public void initPositionButtons() {
        ButtonWidget posTypeButton = addButton(new ButtonWidget(width / 2 - 80, height / 2 + 6, 160, 20, new TranslatableText("speedrunigt.option.timer_position.split_position_type").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.split_position_type."+currentPosType.name().toLowerCase(Locale.ROOT))), (button) -> {
            int order = (currentPosType.ordinal() + 1) % PositionType.values().length;
            currentPosType = PositionType.values()[order];
            changed = true;
            refreshPosition();
            button.setMessage(new TranslatableText("speedrunigt.option.timer_position.split_position_type").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.split_position_type."+currentPosType.name().toLowerCase(Locale.ROOT))));
        }));
        posTypeButton.active = splitPosition;

        posOptions.add(
                addButton(new ButtonWidget(width / 2 - 80, height / 2 - 16, 160, 20, new TranslatableText("speedrunigt.option.timer_position.split_position").append(" : ").append(splitPosition ? ScreenTexts.ON : ScreenTexts.OFF), (button) -> {
                    splitPosition = !splitPosition;
                    changed = true;
                    posTypeButton.active = splitPosition;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.split_position").append(" : ").append(splitPosition ? ScreenTexts.ON : ScreenTexts.OFF));
                }))
        );

        posOptions.add(posTypeButton);
    }

    public void initFontButtons() {
        ButtonWidget prevButton = addButton(new ButtonWidget(width / 2 - 180, height / 2 + 6, 20, 20, new LiteralText("<"), (ButtonWidget button) -> {
            fontPage--;
            openFontPage();
        }));

        ButtonWidget nextButton = addButton(new ButtonWidget(width / 2 + 180, height / 2 + 6, 20, 20, new LiteralText(">"), (ButtonWidget button) -> {
            fontPage++;
            openFontPage();
        }));
        fontOptions.add(prevButton);
        fontOptions.add(nextButton);

        fontSelectButtons.add(
                addButton(new ButtonWidget(width / 2 + 35, height / 2 - 16, 50, 20, new TranslatableText("speedrunigt.option.select"), (ButtonWidget button) -> {
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
                addButton(new ButtonWidget(width / 2 + 35, height / 2 + 6, 50, 20, new TranslatableText("speedrunigt.option.select"), (ButtonWidget button) -> {
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
                addButton(new ButtonWidget(width / 2 + 35, height / 2 + 28, 50, 20, new TranslatableText("speedrunigt.option.select"), (ButtonWidget button) -> {
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


        fontConfigButton = addButton(new ButtonWidget(width / 2 + 88, 0, 50, 20, new LiteralText("Config"), (ButtonWidget button) -> {
            if (client != null) client.openScreen(new FontConfigScreen(this, drawer.getTimerFont()));
        } ));
        fontOptions.add(addButton(new ButtonWidget(width / 2 - 154, height / 2 - 80, 150, 20, new TranslatableText("speedrunigt.option.timer_position.font.open_folder"), (ButtonWidget button) -> Util.getOperatingSystem().open(SpeedRunIGT.FONT_PATH.toFile()))));
        fontOptions.add(addButton(new ButtonWidget(width / 2 + 4, height / 2 - 80, 150, 20, new TranslatableText("speedrunigt.option.timer_position.font.description"), (ButtonWidget button) -> Util.getOperatingSystem().open("https://youtu.be/agBbiTQWj78"))));
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
                addButton(new SliderWidget(width / 2 - 80, height / 2 - 16, 160, 20, new TranslatableText("speedrunigt.option.timer_position.background_opacity").append(" : ").append((int) (drawer.getBGOpacity()*100) + "%"), drawer.getBGOpacity()) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.background_opacity").append(" : ").append((int) (drawer.getBGOpacity()*100) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setBGOpacity((float) this.value);
                        changed = true;
                    }
                })
        );

        backgroundOptions.add(
                addButton(new SliderWidget(width / 2 - 80, height / 2 + 6, 160, 20, new TranslatableText("speedrunigt.option.timer_position.background_padding", "RTA").append(" : ").append(String.valueOf(drawer.getRTAPadding())), (drawer.getRTAPadding()-1) / 24f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.background_padding", "RTA").append(" : ").append(String.valueOf(drawer.getRTAPadding())));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setRTAPadding((int) (this.value * 24) + 1);
                        changed = true;
                    }
                })
        );

        backgroundOptions.add(
                addButton(new SliderWidget(width / 2 - 80, height / 2 + 28, 160, 20, new TranslatableText("speedrunigt.option.timer_position.background_padding", "IGT").append(" : ").append(String.valueOf(drawer.getIGTPadding())), (drawer.getIGTPadding()-1) / 24f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.background_padding", "IGT").append(" : ").append(String.valueOf(drawer.getIGTPadding())));
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
