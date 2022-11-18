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
    private int currentTab = 0;
    private final ArrayList<Identifier> availableFonts = new ArrayList<>();
    private final ArrayList<ButtonWidget> fontSelectButtons = new ArrayList<>();
    private boolean splitPosition = SpeedRunOption.getOption(SpeedRunOptions.ENABLE_TIMER_SPLIT_POS);
    
    public TimerCustomizeScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.option.timer_position"));
        this.parent = parent;
    }
    
    private void openTab(int tab) {
        this.currentTab = tab;
        this.normalButton.active = tab != 0;
        this.igtButton.active = tab != 1;
        this.rtaButton.active = tab != 2;
        this.posButton.active = tab != 5;
        this.fontButton.active = tab != 3;
        this.backgroundButton.active = tab != 4;

        if (this.hide) return;
        for (AbstractButtonWidget normalOption : this.normalOptions) {
            normalOption.visible = tab == 0;
        }
        for (AbstractButtonWidget igtOption : this.igtOptions) {
            igtOption.visible = tab == 1;
        }
        for (AbstractButtonWidget rtaOption : this.rtaOptions) {
            rtaOption.visible = tab == 2;
        }
        for (AbstractButtonWidget fontOption : this.fontOptions) {
            fontOption.visible = tab == 3;
        }
        for (AbstractButtonWidget backgroundOption : this.backgroundOptions) {
            backgroundOption.visible = tab == 4;
        }
        for (AbstractButtonWidget posOption : this.posOptions) {
            posOption.visible = tab == 5;
        }

        this.fontConfigButton.visible = tab == 3 && this.drawer.getTimerFont().getNamespace().equals(SpeedRunIGT.MOD_ID);
    }

    @Override
    protected void init() {
        this.normalOptions.clear();
        this.igtOptions.clear();
        this.rtaOptions.clear();
        this.posOptions.clear();
        this.fontOptions.clear();
        this.availableFonts.clear();
        this.fontSelectButtons.clear();
        this.backgroundOptions.clear();

        if (this.client != null) {
            FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftClientAccessor) this.client).getFontManager();
            if (!fontManager.getFontStorages().containsKey(this.drawer.getTimerFont())) {
                this.availableFonts.add(this.drawer.getTimerFont());
            }

            this.availableFonts.addAll(fontManager.getFontStorages().keySet());
        }

        this.initNormal();
        this.initIGTButtons();
        this.initRTAButtons();
        this.initPositionButtons();
        this.initFontButtons();
        this.initBackgroundButtons();

        this.normalButton = this.addButton(new ButtonWidget(this.width / 2 - 179, this.height / 2 - 48, 58, 20, new TranslatableText("options.title").append("..."), (ButtonWidget button) -> this.openTab(0)));
        this.tabButtons.add(this.normalButton);

        this.igtButton = this.addButton(new ButtonWidget(this.width / 2 - 119, this.height / 2 - 48, 58, 20, new LiteralText("IGT..."), (ButtonWidget button) -> this.openTab(1)));
        this.tabButtons.add(this.igtButton);

        this.rtaButton = this.addButton(new ButtonWidget(this.width / 2 - 59, this.height / 2 - 48, 58, 20, new LiteralText("RTA..."), (ButtonWidget button) -> this.openTab(2)));
        this.tabButtons.add(this.rtaButton);

        this.posButton = this.addButton(new ButtonWidget(this.width / 2 + 1, this.height / 2 - 48, 58, 20, new LiteralText("Pos..."), (ButtonWidget button) -> this.openTab(5)));
        this.tabButtons.add(this.posButton);

        this.fontButton = this.addButton(new ButtonWidget(this.width / 2 + 61, this.height / 2 - 48, 58, 20, new TranslatableText("speedrunigt.title.font"), (ButtonWidget button) -> {
            this.openTab(3);
            this.openFontPage();
        }));
        this.tabButtons.add(this.fontButton);

        this.backgroundButton = this.addButton(new ButtonWidget(this.width / 2 + 121, this.height / 2 - 48, 58, 20, new TranslatableText("speedrunigt.title.background"), (ButtonWidget button) -> this.openTab(4)));
        this.tabButtons.add(this.backgroundButton);

        this.addButton(new ButtonWidget(this.width / 2 - 89, this.height / 2 + 62, 58, 20, new TranslatableText("speedrunigt.option.this.hide"), (ButtonWidget button) -> {
            this.hide = !this.hide;
            for (AbstractButtonWidget normalOption : this.normalOptions) {
                normalOption.visible = !this.hide && this.currentTab == 0;
            }
            for (AbstractButtonWidget igtOption : this.igtOptions) {
                igtOption.visible = !this.hide && this.currentTab == 1;
            }
            for (AbstractButtonWidget rtaOption : this.rtaOptions) {
                rtaOption.visible = !this.hide && this.currentTab == 2;
            }
            for (AbstractButtonWidget posOption : this.posOptions) {
                posOption.visible = !this.hide && this.currentTab == 5;
            }
            for (AbstractButtonWidget fontOption : this.fontOptions) {
                fontOption.visible = !this.hide && this.currentTab == 3;
            }
            for (AbstractButtonWidget backgroundOption : this.backgroundOptions) {
                backgroundOption.visible = !this.hide && this.currentTab == 4;
            }
            for (AbstractButtonWidget tabButton : this.tabButtons) {
                tabButton.visible = !this.hide;
            }
            this.openTab(this.currentTab);
            button.setMessage(new TranslatableText("speedrunigt.option." + (!this.hide ? "this.hide" : "show")));
        }));

        this.saveButton = this.addButton(new ButtonWidget(this.width / 2 - 29, this.height / 2 + 62, 58, 20, new TranslatableText("selectWorld.edit.save"), (ButtonWidget button) -> {
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_SCALE, this.drawer.getIGTScale());
            SpeedRunIGTClient.TIMER_DRAWER.setIGTScale(this.drawer.getIGTScale());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_COLOR, this.drawer.getIGTColor());
            SpeedRunIGTClient.TIMER_DRAWER.setIGTColor(this.drawer.getIGTColor());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_DECO, this.drawer.getIGTDecoration());
            SpeedRunIGTClient.TIMER_DRAWER.setIGTDecoration(this.drawer.getIGTDecoration());

            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_SCALE, this.drawer.getRTAScale());
            SpeedRunIGTClient.TIMER_DRAWER.setRTAScale(this.drawer.getRTAScale());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_COLOR, this.drawer.getRTAColor());
            SpeedRunIGTClient.TIMER_DRAWER.setRTAColor(this.drawer.getRTAColor());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_DECO, this.drawer.getRTADecoration());
            SpeedRunIGTClient.TIMER_DRAWER.setRTADecoration(this.drawer.getRTADecoration());

            SpeedRunOption.setOption(SpeedRunOptions.DISPLAY_TIME_ONLY, this.drawer.isSimplyTimer());
            SpeedRunIGTClient.TIMER_DRAWER.setSimplyTimer(this.drawer.isSimplyTimer());
            SpeedRunOption.setOption(SpeedRunOptions.LOCK_TIMER_POSITION, this.drawer.isLocked());
            SpeedRunIGTClient.TIMER_DRAWER.setLocked(this.drawer.isLocked());
            SpeedRunOption.setOption(SpeedRunOptions.DISPLAY_DECIMALS, this.drawer.getTimerDecimals());
            SpeedRunIGTClient.TIMER_DRAWER.setTimerDecimals(this.drawer.getTimerDecimals());

            SpeedRunOption.setOption(SpeedRunOptions.TIMER_TEXT_FONT, this.drawer.getTimerFont());
            SpeedRunIGTClient.TIMER_DRAWER.setTimerFont(this.drawer.getTimerFont());

            SpeedRunOption.setOption(SpeedRunOptions.BACKGROUND_OPACITY, this.drawer.getBGOpacity());
            SpeedRunIGTClient.TIMER_DRAWER.setBGOpacity(this.drawer.getBGOpacity());
            SpeedRunOption.setOption(SpeedRunOptions.RTA_BACKGROUND_PADDING, this.drawer.getRTAPadding());
            SpeedRunIGTClient.TIMER_DRAWER.setRTAPadding(this.drawer.getRTAPadding());
            SpeedRunOption.setOption(SpeedRunOptions.IGT_BACKGROUND_PADDING, this.drawer.getIGTPadding());
            SpeedRunIGTClient.TIMER_DRAWER.setIGTPadding(this.drawer.getIGTPadding());

            for (Map.Entry<PositionType, Vec2f> igtPosEntry : this.posTypesIGT.entrySet()) {
                if (igtPosEntry.getKey() == PositionType.DEFAULT) {
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_POSITION_X, igtPosEntry.getValue().x);
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_POSITION_Y, igtPosEntry.getValue().y);
                } else {
                    SpeedRunOption.setOption(igtPosEntry.getKey() == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_IGT_POSITION_FOR_F3 : SpeedRunOptions.TIMER_IGT_POSITION_FOR_PAUSE, igtPosEntry.getValue());
                }
            }

            for (Map.Entry<PositionType, Vec2f> rtaPosEntry : this.posTypesRTA.entrySet()) {
                if (rtaPosEntry.getKey() == PositionType.DEFAULT) {
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_POSITION_X, rtaPosEntry.getValue().x);
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_POSITION_Y, rtaPosEntry.getValue().y);
                } else {
                    SpeedRunOption.setOption(rtaPosEntry.getKey() == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_RTA_POSITION_FOR_F3 : SpeedRunOptions.TIMER_RTA_POSITION_FOR_PAUSE, rtaPosEntry.getValue());
                }
            }

            SpeedRunIGTClient.TIMER_DRAWER.update();
            SpeedRunOption.setOption(SpeedRunOptions.ENABLE_TIMER_SPLIT_POS, splitPosition);

            this.changed = false;
        }));

        this.addButton(new ButtonWidget(this.width / 2 + 31, this.height / 2 + 62, 58, 20, ScreenTexts.CANCEL, (ButtonWidget button) -> {
            if (this.client != null) this.client.openScreen(this.parent);
        }));

        this.fontConfigButton.visible = false;

        this.openTab(0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean isClicked = super.mouseClicked(mouseX, mouseY, button);
        if (!isClicked && button == 0 && !this.drawer.isLocked()) {
            if (!this.igtButton.active) {
                this.drawer.setIGT_XPos(MathHelper.clamp((float) (mouseX / this.width), 0, 1));
                this.drawer.setIGT_YPos(MathHelper.clamp((float) (mouseY / this.height), 0, 1));
                this.posTypesIGT.put(this.currentPosType, new Vec2f(this.drawer.getIGT_XPos(), this.drawer.getIGT_YPos()));
                this.changed = true;
            }
            if (!this.rtaButton.active) {
                this.drawer.setRTA_XPos(MathHelper.clamp((float) (mouseX / this.width), 0, 1));
                this.drawer.setRTA_YPos(MathHelper.clamp((float) (mouseY / this.height), 0, 1));
                this.posTypesRTA.put(this.currentPosType, new Vec2f(this.drawer.getRTA_XPos(), this.drawer.getRTA_YPos()));
                this.changed = true;
            }
        }
        return isClicked;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (modifiers == 2 && keyCode >= 262 && keyCode <= 265 && this.client != null && !this.drawer.isLocked()) {
            int moveX = keyCode == 262 ? 1 : keyCode == 263 ? -1 : 0;
            int moveY = keyCode == 265 ? -1 : keyCode == 264 ? 1 : 0;
            if (!this.igtButton.active) {
                this.drawer.setIGT_XPos(MathHelper.clamp(this.drawer.getIGT_XPos() + moveX * this.drawer.getIGTScale() / this.client.getWindow().getScaledWidth(), 0, 1));
                this.drawer.setIGT_YPos(MathHelper.clamp(this.drawer.getIGT_YPos() + moveY * this.drawer.getIGTScale() / this.client.getWindow().getScaledHeight(), 0, 1));
                this.posTypesIGT.put(this.currentPosType, new Vec2f(this.drawer.getIGT_XPos(), this.drawer.getIGT_YPos()));
                this.changed = true;
            }
            if (!this.rtaButton.active) {
                this.drawer.setRTA_XPos(MathHelper.clamp(this.drawer.getRTA_XPos() + moveX * this.drawer.getRTAScale() / this.client.getWindow().getScaledWidth(), 0, 1));
                this.drawer.setRTA_YPos(MathHelper.clamp(this.drawer.getRTA_YPos() + moveY * this.drawer.getRTAScale() / this.client.getWindow().getScaledHeight(), 0, 1));
                this.posTypesRTA.put(this.currentPosType, new Vec2f(this.drawer.getRTA_XPos(), this.drawer.getRTA_YPos()));
                this.changed = true;
            }
            setFocused(null);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        this.saveButton.active = this.changed;
        this.drawer.draw();
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);

        if (!this.hide) {
            if (!this.igtButton.active || !this.rtaButton.active) {
                if (this.drawer.isLocked()) {
                    this.drawCenteredText(matrices, this.textRenderer, new TranslatableText("speedrunigt.option.timer_position.description.lock"), this.width / 2, this.height / 2 - 80, 16777215);
                } else {
                    this.drawCenteredText(matrices, this.textRenderer, new TranslatableText("speedrunigt.option.timer_position.description"), this.width / 2, this.height / 2 - 80, 16777215);
                    this.drawCenteredText(matrices, this.textRenderer, new TranslatableText("speedrunigt.option.timer_position.description.move"), this.width / 2, this.height / 2 - 69, 16777215);
                }
            }

            if (!this.fontButton.active && this.client != null) {
                int c = this.fontPage * 3;
                FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftClientAccessor) this.client).getFontManager();
                for (int i = 0; i < this.fontSelectButtons.size(); i++) {
                    if (c + i < this.availableFonts.size()) {
                        Identifier fontIdentifier = this.availableFonts.get(c + i);
                        LiteralText text = new LiteralText(fontIdentifier.getPath());

                        if (this.client != null && fontManager.getFontStorages().containsKey(fontIdentifier) && !SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE)) {
                            text.setStyle(text.getStyle().withFont(fontIdentifier));
                        } else {
                            text.append(new LiteralText(" (Unavailable)")).formatted(Formatting.RED);
                        }

                        if (fontIdentifier.toString().equals(this.drawer.getTimerFont().toString())) {
                            text.append(" [Selected]").formatted(Formatting.ITALIC);
                        }
                        
                        this.drawCenteredText(matrices, this.textRenderer, text, this.width / 2 - 30, this.height / 2 - 11 + (i * 22), 16777215);
                    }
                }
            }
        }
        
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (this.client != null) {
            this.client.openScreen(this.parent);
        }
    }

    private void refreshPosition() {
        Vec2f igtPos, rtaPos;
        
        if (this.posTypesIGT.containsKey(this.currentPosType)) {
            igtPos = this.posTypesIGT.get(this.currentPosType);
        } else {
            igtPos = this.currentPosType == PositionType.DEFAULT
                        ? new Vec2f(SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_X), SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_Y))
                        : SpeedRunOption.getOption(this.currentPosType == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_IGT_POSITION_FOR_F3 : SpeedRunOptions.TIMER_IGT_POSITION_FOR_PAUSE);
        }
        
        if (this.posTypesRTA.containsKey(this.currentPosType)) {
            rtaPos = this.posTypesRTA.get(this.currentPosType);
        } else {
            rtaPos = this.currentPosType == PositionType.DEFAULT
                        ? new Vec2f(SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_X), SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_Y))
                        : SpeedRunOption.getOption(this.currentPosType == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_RTA_POSITION_FOR_F3 : SpeedRunOptions.TIMER_RTA_POSITION_FOR_PAUSE);
        }
        
        this.drawer.setIGT_XPos(igtPos.x);
        this.drawer.setIGT_YPos(igtPos.y);
        this.drawer.setRTA_XPos(rtaPos.x);
        this.drawer.setRTA_YPos(rtaPos.y);
    }

    public void initNormal() {
        this.normalOptions.add(
                this.addButton(new ButtonWidget(this.width / 2 - 80, this.height / 2 - 16, 160, 20, new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(this.drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
                    this.drawer.setSimplyTimer(!this.drawer.isSimplyTimer());
                    this.changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.show_time_only").append(" : ").append(this.drawer.isSimplyTimer() ? ScreenTexts.ON : ScreenTexts.OFF));
                }))
        );

        this.normalOptions.add(
                this.addButton(new ButtonWidget(this.width / 2 - 80, this.height / 2 + 6, 160, 20, new TranslatableText("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(this.drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF), (ButtonWidget button) -> {
                    this.drawer.setLocked(!this.drawer.isLocked());
                    this.changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(this.drawer.isLocked() ? ScreenTexts.ON : ScreenTexts.OFF));
                }))
        );

        this.normalOptions.add(
                this.addButton(new ButtonWidget(this.width / 2 - 80, this.height / 2 + 28, 160, 20, new TranslatableText("speedrunigt.option.timer_position.show_decimals").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.show_decimals.context", this.drawer.getTimerDecimals().getNumber())), (ButtonWidget button) -> {
                    int order = this.drawer.getTimerDecimals().ordinal();
                    this.drawer.setTimerDecimals(TimerDecimals.values()[(++order) % TimerDecimals.values().length]);
                    this.changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.show_decimals").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.show_decimals.context", this.drawer.getTimerDecimals().getNumber())));
                }, (button, matrices, mouseX, mouseY) -> renderTooltip(matrices, new TranslatableText("speedrunigt.option.timer_position.show_decimals.description"), mouseX, mouseY)))
        );
    }

    public void initIGTButtons() {
        this.igtOptions.add(
                this.addButton(new SliderWidget(this.width / 2 - 127, this.height / 2 - 16, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_red", "IGT").append(" : ").append(String.valueOf(ColorMixer.getRed(this.drawer.getIGTColor()))), ColorMixer.getRed(this.drawer.getIGTColor()) / 255.0f) {
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

        this.igtOptions.add(
                this.addButton(new SliderWidget(this.width / 2 - 127, this.height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_green", "IGT").append(" : ").append(String.valueOf(ColorMixer.getGreen(this.drawer.getIGTColor()))), ColorMixer.getGreen(this.drawer.getIGTColor()) / 255.0f) {
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

        this.igtOptions.add(
                this.addButton(new SliderWidget(this.width / 2 - 127, this.height / 2 + 28, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_blue", "IGT").append(" : ").append(String.valueOf(ColorMixer.getBlue(this.drawer.getIGTColor()))), ColorMixer.getBlue(this.drawer.getIGTColor()) / 255.0f) {
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

        this.igtOptions.add(
                this.addButton(new SliderWidget(this.width / 2 + 6, this.height / 2 - 16, 120, 20, new TranslatableText("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append((Math.round(this.drawer.getIGTScale() * 100)) + "%"), this.drawer.getIGTScale() / 3f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append((Math.round(drawer.getIGTScale() * 100)) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setIGTScale(Math.round((float) this.value * 3f * 20f)/20f);
                        changed = true;
                    }
                })
        );

        this.igtOptions.add(
                this.addButton(new ButtonWidget(this.width / 2 + 6, this.height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_decorate", "IGT").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + this.drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))), (ButtonWidget button) -> {
                    int order = this.drawer.getIGTDecoration().ordinal();
                    this.drawer.setIGTDecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    this.changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.text_decorate", "IGT").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + this.drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))));
                }))
        );
    }

    public void initRTAButtons() {
        this.rtaOptions.add(
                this.addButton(new SliderWidget(this.width / 2 - 127, this.height / 2 - 16, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_red", "RTA").append(" : ").append(String.valueOf(ColorMixer.getRed(this.drawer.getRTAColor()))), ColorMixer.getRed(this.drawer.getRTAColor()) / 255.0f) {
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

        this.rtaOptions.add(
                this.addButton(new SliderWidget(this.width / 2 - 127, this.height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_green", "RTA").append(" : ").append(String.valueOf(ColorMixer.getGreen(this.drawer.getRTAColor()))), ColorMixer.getGreen(this.drawer.getRTAColor()) / 255.0f) {
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

        this.rtaOptions.add(
                this.addButton(new SliderWidget(this.width / 2 - 127, this.height / 2 + 28, 120, 20, new TranslatableText("speedrunigt.option.timer_position.color_blue", "RTA").append(" : ").append(String.valueOf(ColorMixer.getBlue(this.drawer.getRTAColor()))), ColorMixer.getBlue(this.drawer.getRTAColor()) / 255.0f) {
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

        this.rtaOptions.add(
                this.addButton(new SliderWidget(this.width / 2 + 6, this.height / 2 - 16, 120, 20, new TranslatableText("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append((Math.round(this.drawer.getRTAScale() * 100)) + "%"), this.drawer.getRTAScale() / 3f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append((Math.round(drawer.getRTAScale() * 100)) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setRTAScale(Math.round((float) this.value * 3f * 20f)/20f);
                        changed = true;
                    }
                })
        );

        this.rtaOptions.add(
                this.addButton(new ButtonWidget(this.width / 2 + 6, this.height / 2 + 6, 120, 20, new TranslatableText("speedrunigt.option.timer_position.text_decorate", "RTA").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + this.drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))), (ButtonWidget button) -> {
                    int order = this.drawer.getRTADecoration().ordinal();
                    this.drawer.setRTADecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    this.changed = true;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.text_decorate", "RTA").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.text_decorate." + this.drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))));
                }))
        );
    }

    public void initPositionButtons() {
        ButtonWidget posTypeButton = this.addButton(new ButtonWidget(this.width / 2 - 80, this.height / 2 + 6, 160, 20, new TranslatableText("speedrunigt.option.timer_position.split_position_type").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.split_position_type." + this.currentPosType.name().toLowerCase(Locale.ROOT))), (button) -> {
            int order = (this.currentPosType.ordinal() + 1) % PositionType.values().length;
            this.currentPosType = PositionType.values()[order];
            this.changed = true;
            this.refreshPosition();
            button.setMessage(new TranslatableText("speedrunigt.option.timer_position.split_position_type").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.split_position_type." + this.currentPosType.name().toLowerCase(Locale.ROOT))));
        }));
        posTypeButton.active = this.splitPosition;

        this.posOptions.add(
                this.addButton(new ButtonWidget(this.width / 2 - 80, this.height / 2 - 16, 160, 20, new TranslatableText("speedrunigt.option.timer_position.split_position").append(" : ").append(this.splitPosition ? ScreenTexts.ON : ScreenTexts.OFF), (button) -> {
                    this.splitPosition = !this.splitPosition;
                    this.changed = true;
                    posTypeButton.active = this.splitPosition;
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position.split_position").append(" : ").append(this.splitPosition ? ScreenTexts.ON : ScreenTexts.OFF));
                    if (!this.splitPosition) {
                        this.currentPosType = PositionType.DEFAULT;
                        this.refreshPosition();
                        posTypeButton.setMessage(new TranslatableText("speedrunigt.option.timer_position.split_position_type").append(" : ").append(new TranslatableText("speedrunigt.option.timer_position.split_position_type." + this.currentPosType.name().toLowerCase(Locale.ROOT))));
                    }
                }))
        );

        this.posOptions.add(posTypeButton);
    }

    public void initFontButtons() {
        ButtonWidget prevButton = this.addButton(new ButtonWidget(this.width / 2 - 180, this.height / 2 + 6, 20, 20, new LiteralText("<"), (ButtonWidget button) -> {
            this.fontPage--;
            this.openFontPage();
        }));

        ButtonWidget nextButton = this.addButton(new ButtonWidget(this.width / 2 + 180, this.height / 2 + 6, 20, 20, new LiteralText(">"), (ButtonWidget button) -> {
            this.fontPage++;
            this.openFontPage();
        }));
        this.fontOptions.add(prevButton);
        this.fontOptions.add(nextButton);

        this.fontSelectButtons.add(
                this.addButton(new ButtonWidget(this.width / 2 + 35, this.height / 2 - 16, 50, 20, new TranslatableText("speedrunigt.option.select"), (ButtonWidget button) -> {
                    int c = (this.fontPage * 3);
                    if (this.availableFonts.size() > c) {
                        for (ButtonWidget fontSelectButton : this.fontSelectButtons) fontSelectButton.active = true;
                        this.drawer.setTimerFont(this.availableFonts.get(c));
                        button.active = false;
                        this.openFontPage();
                        this.changed = true;
                    }
                }))
        );
        this.fontSelectButtons.add(
                this.addButton(new ButtonWidget(this.width / 2 + 35, this.height / 2 + 6, 50, 20, new TranslatableText("speedrunigt.option.select"), (ButtonWidget button) -> {
                    int c = (this.fontPage * 3) + 1;
                    if (this.availableFonts.size() > c) {
                        for (ButtonWidget fontSelectButton : this.fontSelectButtons) fontSelectButton.active = true;
                        this.drawer.setTimerFont(this.availableFonts.get(c));
                        button.active = false;
                        this.openFontPage();
                        this.changed = true;
                    }
                }))
        );
        this.fontSelectButtons.add(
                this.addButton(new ButtonWidget(this.width / 2 + 35, this.height / 2 + 28, 50, 20, new TranslatableText("speedrunigt.option.select"), (ButtonWidget button) -> {
                    int c = (this.fontPage * 3) + 2;
                    if (this.availableFonts.size() > c) {
                        for (ButtonWidget fontSelectButton : this.fontSelectButtons) fontSelectButton.active = true;
                        this.drawer.setTimerFont(this.availableFonts.get(c));
                        button.active = false;
                        this.openFontPage();
                        this.changed = true;
                    }
                }))
        );
        for (AbstractButtonWidget fontOption : this.fontOptions) {
            fontOption.visible = false;
        }
        for (ButtonWidget fontSelectButton : this.fontSelectButtons) {
            fontSelectButton.visible = false;
        }
        this.fontOptions.addAll(this.fontSelectButtons);


        this.fontConfigButton = this.addButton(new ButtonWidget(this.width / 2 + 88, 0, 50, 20, new LiteralText("Config"), (ButtonWidget button) -> {
            if (client != null) client.openScreen(new FontConfigScreen(this, this.drawer.getTimerFont()));
        } ));
        this.fontOptions.add(this.addButton(new ButtonWidget(this.width / 2 - 154, this.height / 2 - 80, 150, 20, new TranslatableText("speedrunigt.option.timer_position.font.open_folder"), (ButtonWidget button) -> Util.getOperatingSystem().open(SpeedRunIGT.FONT_PATH.toFile()))));
        this.fontOptions.add(this.addButton(new ButtonWidget(this.width / 2 + 4, this.height / 2 - 80, 150, 20, new TranslatableText("speedrunigt.option.timer_position.font.description"), (ButtonWidget button) -> Util.getOperatingSystem().open("https://youtu.be/agBbiTQWj78"))));
        this.openFontPage();
    }

    public void openFontPage() {
        this.fontOptions.get(0).active = this.fontPage != 0;
        this.fontOptions.get(1).active = this.fontPage != Math.max((this.availableFonts.size() - 1) / 3, 0);

        int c = this.fontPage * 3;
        int available = 0;
        for (int i = 0; i < this.fontSelectButtons.size(); i++) {
            ButtonWidget button = this.fontSelectButtons.get(i);
            if (c + i < this.availableFonts.size()) {
                button.active = !this.availableFonts.get(c + i).toString().equals(this.drawer.getTimerFont().toString());
                if (!button.active && Objects.equals(this.drawer.getTimerFont().getNamespace(), SpeedRunIGT.MOD_ID)) available = button.y;
                button.visible = true;
            } else {
                button.visible = false;
            }
        }

        this.fontConfigButton.visible = this.currentTab == 3 && available != 0;
        this.fontConfigButton.y = available;
    }

    public void initBackgroundButtons() {
        this.backgroundOptions.add(
                this.addButton(new SliderWidget(this.width / 2 - 80, this.height / 2 - 16, 160, 20, new TranslatableText("speedrunigt.option.timer_position.background_opacity").append(" : ").append((int) (this.drawer.getBGOpacity()*100) + "%"), this.drawer.getBGOpacity()) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(new TranslatableText("speedrunigt.option.timer_position.background_opacity").append(" : ").append((int) (drawer.getBGOpacity() * 100) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setBGOpacity((float) this.value);
                        changed = true;
                    }
                })
        );

        this.backgroundOptions.add(
                this.addButton(new SliderWidget(this.width / 2 - 80, this.height / 2 + 6, 160, 20, new TranslatableText("speedrunigt.option.timer_position.background_padding", "RTA").append(" : ").append(String.valueOf(this.drawer.getRTAPadding())), (this.drawer.getRTAPadding() - 1) / 24f) {
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

        this.backgroundOptions.add(
                this.addButton(new SliderWidget(this.width / 2 - 80, this.height / 2 + 28, 160, 20, new TranslatableText("speedrunigt.option.timer_position.background_padding", "IGT").append(" : ").append(String.valueOf(this.drawer.getIGTPadding())), (this.drawer.getIGTPadding() - 1) / 24f) {
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
