package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTClient;
import com.redlimerl.speedrunigt.mixins.access.FontManagerAccessor;
import com.redlimerl.speedrunigt.mixins.access.MinecraftAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecimals;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDecoration;
import com.redlimerl.speedrunigt.option.SpeedRunOptions.TimerDisplayAlign;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.timer.TimerDrawer.PositionType;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class TimerCustomizeScreen extends Screen {

    private final TimerDrawer drawer = new TimerDrawer(false);
    private final Screen parent;

    private PositionType currentPosType = PositionType.DEFAULT;
    private final HashMap<PositionType, Vec2> posTypesRTA = new HashMap<>();
    private final HashMap<PositionType, Vec2> posTypesIGT = new HashMap<>();

    private boolean changed = false;
    private boolean hide = false;
    private final ArrayList<AbstractWidget> tabButtons = new ArrayList<>();
    private final ArrayList<AbstractWidget> normalOptions = new ArrayList<>();
    private final ArrayList<AbstractWidget> igtOptions = new ArrayList<>();
    private final ArrayList<AbstractWidget> rtaOptions = new ArrayList<>();
    private final ArrayList<AbstractWidget> posOptions = new ArrayList<>();
    private final ArrayList<AbstractWidget> fontOptions = new ArrayList<>();
    private final ArrayList<AbstractWidget> backgroundOptions = new ArrayList<>();
    private Button normalButton;
    private Button igtButton;
    private Button rtaButton;
    private Button posButton;
    private Button fontButton;
    private Button backgroundButton;
    private Button saveButton;
    private Button fontConfigButton;

    private int fontPage = 0;
    private final ArrayList<Identifier> availableFonts = new ArrayList<>();
    private final ArrayList<Button> fontSelectButtons = new ArrayList<>();

    private boolean splitPosition = SpeedRunOption.getOption(SpeedRunOptions.ENABLE_TIMER_SPLIT_POS);

    public TimerCustomizeScreen(Screen parent) {
        super(Component.translatable("speedrunigt.option.timer_position"));
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
        for (AbstractWidget normalOption : normalOptions) {
            normalOption.visible = tab == 0;
        }
        for (AbstractWidget igtOption : igtOptions) {
            igtOption.visible = tab == 1;
        }
        for (AbstractWidget rtaOption : rtaOptions) {
            rtaOption.visible = tab == 2;
        }
        for (AbstractWidget fontOption : fontOptions) {
            fontOption.visible = tab == 3;
        }
        for (AbstractWidget backgroundOption : backgroundOptions) {
            backgroundOption.visible = tab == 4;
        }
        for (AbstractWidget posOption : posOptions) {
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

        if (minecraft != null) {
            FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftAccessor) minecraft).getFontManager();
            if (!fontManager.getFontSets().containsKey(drawer.getTimerFont())) {
                availableFonts.add(drawer.getTimerFont());
            }

            availableFonts.addAll(fontManager.getFontSets().keySet());
        }

        initNormal();
        initIGTButtons();
        initRTAButtons();
        initPositionButtons();
        initFontButtons();
        initBackgroundButtons();

        this.normalButton = addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 179, height / 2 - 48, 58, 20, Component.translatable("options.title").append("..."), (Button button) -> openTab(0)));
        this.tabButtons.add(this.normalButton);

        this.igtButton = addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 119, height / 2 - 48, 58, 20, Component.literal("IGT..."), (Button button) -> openTab(1)));
        this.tabButtons.add(this.igtButton);

        this.rtaButton = addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 59, height / 2 - 48, 58, 20, Component.literal("RTA..."), (Button button) -> openTab(2)));
        this.tabButtons.add(this.rtaButton);

        this.posButton = addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 1, height / 2 - 48, 58, 20, Component.literal("Pos..."), (Button button) -> openTab(5)));
        this.tabButtons.add(this.posButton);

        this.fontButton = addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 61, height / 2 - 48, 58, 20, Component.translatable("speedrunigt.title.font"), (Button button) -> {
            openTab(3);
            openFontPage();
        }));
        this.tabButtons.add(this.fontButton);

        this.backgroundButton = addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 121, height / 2 - 48, 58, 20, Component.translatable("speedrunigt.title.background"), (Button button) -> openTab(4)));
        this.tabButtons.add(this.backgroundButton);


        addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 89, height / 2 + 62, 58, 20, Component.translatable("speedrunigt.option.hide"), (Button button) -> {
            hide = !hide;
            for (AbstractWidget normalOption : normalOptions) {
                normalOption.visible = !hide && currentTab == 0;
            }
            for (AbstractWidget igtOption : igtOptions) {
                igtOption.visible = !hide && currentTab == 1;
            }
            for (AbstractWidget rtaOption : rtaOptions) {
                rtaOption.visible = !hide && currentTab == 2;
            }
            for (AbstractWidget posOption : posOptions) {
                posOption.visible = !hide && currentTab == 5;
            }
            for (AbstractWidget fontOption : fontOptions) {
                fontOption.visible = !hide && currentTab == 3;
            }
            for (AbstractWidget backgroundOption : backgroundOptions) {
                backgroundOption.visible = !hide && currentTab == 4;
            }
            for (AbstractWidget tabButton : tabButtons) {
                tabButton.visible = !hide;
            }
            openTab(currentTab);
            button.setMessage(Component.translatable("speedrunigt.option." + (!hide ? "hide" : "show")));
        }));

        this.saveButton = addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 29, height / 2 + 62, 58, 20, Component.translatable("selectWorld.edit.save"), (Button button) -> {
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_SCALE, drawer.getIGTScale());
            SpeedRunIGTClient.TIMER_DRAWER.setIGTScale(drawer.getIGTScale());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_COLOR, drawer.getIGTColor());
            SpeedRunIGTClient.TIMER_DRAWER.setIGTColor(drawer.getIGTColor());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_DECO, drawer.getIGTDecoration());
            SpeedRunIGTClient.TIMER_DRAWER.setIGTDecoration(drawer.getIGTDecoration());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_DISPLAY_ALIGN, drawer.getIGTDisplayAlign());
            SpeedRunIGTClient.TIMER_DRAWER.setIGTDisplayAlign(drawer.getIGTDisplayAlign());

            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_SCALE, drawer.getRTAScale());
            SpeedRunIGTClient.TIMER_DRAWER.setRTAScale(drawer.getRTAScale());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_COLOR, drawer.getRTAColor());
            SpeedRunIGTClient.TIMER_DRAWER.setRTAColor(drawer.getRTAColor());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_DECO, drawer.getRTADecoration());
            SpeedRunIGTClient.TIMER_DRAWER.setRTADecoration(drawer.getRTADecoration());
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_RTA_DISPLAY_ALIGN, drawer.getRTADisplayAlign());
            SpeedRunIGTClient.TIMER_DRAWER.setRTADisplayAlign(drawer.getRTADisplayAlign());

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

            for (Map.Entry<PositionType, Vec2> igtPosEntry : posTypesIGT.entrySet()) {
                if (igtPosEntry.getKey() == PositionType.DEFAULT) {
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_POSITION_X, igtPosEntry.getValue().x);
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_IGT_POSITION_Y, igtPosEntry.getValue().y);
                } else {
                    SpeedRunOption.setOption(igtPosEntry.getKey() == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_IGT_POSITION_FOR_F3 : SpeedRunOptions.TIMER_IGT_POSITION_FOR_PAUSE, igtPosEntry.getValue());
                }
            }

            for (Map.Entry<PositionType, Vec2> rtaPosEntry : posTypesRTA.entrySet()) {
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
        }));

        addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 31, height / 2 + 62, 58, 20, CommonComponents.GUI_CANCEL, (Button button) -> {
            if (minecraft != null) minecraft.gui.setScreen(parent);
        }));

        fontConfigButton.visible = false;

        openTab(0);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        boolean isClicked = super.mouseClicked(click, doubled);
        if (!isClicked && click.button() == 0 && !drawer.isLocked()) {
            if (!this.igtButton.active) {
                drawer.setIGT_XPos(Mth.clamp((float) (click.x() / width), 0, 1));
                drawer.setIGT_YPos(Mth.clamp((float) (click.y() / height), 0, 1));
                posTypesIGT.put(currentPosType, new Vec2(drawer.getIGT_XPos(), drawer.getIGT_YPos()));
                changed = true;
            }
            if (!this.rtaButton.active) {
                drawer.setRTA_XPos(Mth.clamp((float) (click.x() / width), 0, 1));
                drawer.setRTA_YPos(Mth.clamp((float) (click.y() / height), 0, 1));
                posTypesRTA.put(currentPosType, new Vec2(drawer.getRTA_XPos(), drawer.getRTA_YPos()));
                changed = true;
            }
        }
        return isClicked;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.modifiers() == 2 && input.input() >= 262 && input.input() <= 265 && minecraft != null && !drawer.isLocked()) {
            int moveX = input.input() == 262 ? 1 : input.input() == 263 ? -1 : 0;
            int moveY = input.input() == 265 ? -1 : input.input() == 264 ? 1 : 0;
            if (!igtButton.active) {
                drawer.setIGT_XPos(Mth.clamp(drawer.getIGT_XPos() + moveX * drawer.getIGTScale() / minecraft.getWindow().getGuiScaledWidth(), 0, 1));
                drawer.setIGT_YPos(Mth.clamp(drawer.getIGT_YPos() + moveY * drawer.getIGTScale() / minecraft.getWindow().getGuiScaledHeight(), 0, 1));
                posTypesIGT.put(currentPosType, new Vec2(drawer.getIGT_XPos(), drawer.getIGT_YPos()));
                changed = true;
            }
            if (!rtaButton.active) {
                drawer.setRTA_XPos(Mth.clamp(drawer.getRTA_XPos() + moveX * drawer.getRTAScale() / minecraft.getWindow().getGuiScaledWidth(), 0, 1));
                drawer.setRTA_YPos(Mth.clamp(drawer.getRTA_YPos() + moveY * drawer.getRTAScale() / minecraft.getWindow().getGuiScaledHeight(), 0, 1));
                posTypesRTA.put(currentPosType, new Vec2(drawer.getRTA_XPos(), drawer.getRTA_YPos()));
                changed = true;
            }
            setFocused(null);
        }
        return super.keyPressed(input);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        saveButton.active = changed;

        drawer.draw(graphics);

        graphics.centeredText(this.font, this.title, this.width / 2, 15, CommonColors.WHITE);

        if (!hide) {
            if (!igtButton.active || !rtaButton.active) {
                if (drawer.isLocked()) {
                    graphics.centeredText(this.font,
                            Component.translatable("speedrunigt.option.timer_position.description.lock"), this.width / 2, this.height / 2 - 80, CommonColors.WHITE);
                } else {
                    graphics.centeredText(this.font,
                            Component.translatable("speedrunigt.option.timer_position.description"), this.width / 2, this.height / 2 - 80, CommonColors.WHITE);
                    graphics.centeredText(this.font,
                            Component.translatable("speedrunigt.option.timer_position.description.move"), this.width / 2, this.height / 2 - 69, CommonColors.WHITE);
                }
            }

            if (!fontButton.active && minecraft != null) {
                int c = fontPage * 3;
                FontManagerAccessor fontManager = (FontManagerAccessor) ((MinecraftAccessor) minecraft).getFontManager();
                for (int i = 0; i < fontSelectButtons.size(); i++) {
                    if (c + i < availableFonts.size()) {
                        Identifier fontIdentifier = availableFonts.get(c + i);
                        MutableComponent text = Component.literal(fontIdentifier.getPath());

                        if (minecraft != null && fontManager.getFontSets().containsKey(fontIdentifier) && !SpeedRunOption.getOption(SpeedRunOptions.CUSTOM_FONT_SAFE_MODE)) {
                            text.setStyle(text.getStyle().withFont(new FontDescription.Resource(fontIdentifier)));
                        } else {
                            text.append(Component.literal(" (Unavailable)")).withStyle(ChatFormatting.RED);
                        }

                        if (fontIdentifier.toString().equals(drawer.getTimerFont().toString())) {
                            text.append(" [Selected]").withStyle(ChatFormatting.ITALIC);
                        }
                        graphics.centeredText(this.font, text, this.width / 2 - 30,
                                this.height / 2 - 11 + (i * 22), CommonColors.WHITE);
                    }
                }
            }
        }
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        minecraft.gui.setScreen(parent);
    }


    private void refreshPosition() {
        Vec2 igtPos, rtaPos;
        if (posTypesIGT.containsKey(currentPosType)) {
            igtPos = posTypesIGT.get(currentPosType);
        } else {
            igtPos = currentPosType == PositionType.DEFAULT
                    ? new Vec2(SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_X), SpeedRunOption.getOption(SpeedRunOptions.TIMER_IGT_POSITION_Y))
                    : SpeedRunOption.getOption(currentPosType == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_IGT_POSITION_FOR_F3 : SpeedRunOptions.TIMER_IGT_POSITION_FOR_PAUSE);
        }
        if (posTypesRTA.containsKey(currentPosType)) {
            rtaPos = posTypesRTA.get(currentPosType);
        } else {
            rtaPos = currentPosType == PositionType.DEFAULT
                    ? new Vec2(SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_X), SpeedRunOption.getOption(SpeedRunOptions.TIMER_RTA_POSITION_Y))
                    : SpeedRunOption.getOption(currentPosType == PositionType.WHILE_F3 ? SpeedRunOptions.TIMER_RTA_POSITION_FOR_F3 : SpeedRunOptions.TIMER_RTA_POSITION_FOR_PAUSE);
        }
        drawer.setIGT_XPos(igtPos.x);
        drawer.setIGT_YPos(igtPos.y);
        drawer.setRTA_XPos(rtaPos.x);
        drawer.setRTA_YPos(rtaPos.y);
    }


    public void initNormal() {
        normalOptions.add(
                addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 80, height / 2 - 16, 160, 20, Component.translatable("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF), (Button button) -> {
                    drawer.setSimplyTimer(!drawer.isSimplyTimer());
                    changed = true;
                    button.setMessage(Component.translatable("speedrunigt.option.timer_position.show_time_only").append(" : ").append(drawer.isSimplyTimer() ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                }))
        );

        normalOptions.add(
                addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 80, height / 2 + 6, 160, 20, Component.translatable("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(drawer.isLocked() ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF), (Button button) -> {
                    drawer.setLocked(!drawer.isLocked());
                    changed = true;
                    button.setMessage(Component.translatable("speedrunigt.option.timer_position.lock_timer_position").append(" : ").append(drawer.isLocked() ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                }))
        );

        normalOptions.add(
                addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 80, height / 2 + 28, 160, 20, Component.translatable("speedrunigt.option.timer_position.show_decimals").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.show_decimals.context", drawer.getTimerDecimals().getNumber())), (Button button) -> {
                    int order = drawer.getTimerDecimals().ordinal();
                    drawer.setTimerDecimals(TimerDecimals.values()[(++order) % TimerDecimals.values().length]);
                    changed = true;
                    button.setMessage(Component.translatable("speedrunigt.option.timer_position.show_decimals").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.show_decimals.context", drawer.getTimerDecimals().getNumber())));
                }, Tooltip.create(Component.translatable("speedrunigt.option.timer_position.show_decimals.description"))))
        );
    }

    public void initIGTButtons() {
        igtOptions.add(
                addRenderableWidget(new AbstractSliderButton(width / 2 - 127, height / 2 - 16, 120, 20, Component.translatable("speedrunigt.option.timer_position.color_red", "IGT").append(" : ").append(String.valueOf(ARGB.red(drawer.getIGTColor()))), ARGB.red(drawer.getIGTColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Component.translatable("speedrunigt.option.timer_position.color_red", "IGT").append(" : ").append(String.valueOf(ARGB.red(drawer.getIGTColor()))));
                    }

                    @Override
                    protected void applyValue() {
                        int color = drawer.getIGTColor();
                        drawer.setIGTColor(
                                ARGB.color(
                                        ARGB.alpha(color),
                                        (int) (this.value * 255),
                                        ARGB.green(color),
                                        ARGB.blue(color)
                                )
                        );
                        changed = true;
                    }
                })
        );

        igtOptions.add(
                addRenderableWidget(new AbstractSliderButton(width / 2 - 127, height / 2 + 6, 120, 20, Component.translatable("speedrunigt.option.timer_position.color_green", "IGT").append(" : ").append(String.valueOf(ARGB.green(drawer.getIGTColor()))), ARGB.green(drawer.getIGTColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Component.translatable("speedrunigt.option.timer_position.color_green", "IGT").append(" : ").append(String.valueOf(ARGB.green(drawer.getIGTColor()))));
                    }

                    @Override
                    protected void applyValue() {
                        int color = drawer.getIGTColor();
                        drawer.setIGTColor(
                                ARGB.color(
                                        ARGB.alpha(color),
                                        ARGB.red(color),
                                        (int) (this.value * 255),
                                        ARGB.blue(color)
                                )
                        );
                        changed = true;
                    }
                })
        );

        igtOptions.add(
                addRenderableWidget(new AbstractSliderButton(width / 2 - 127, height / 2 + 28, 120, 20, Component.translatable("speedrunigt.option.timer_position.color_blue", "IGT").append(" : ").append(String.valueOf(ARGB.blue(drawer.getIGTColor()))), ARGB.blue(drawer.getIGTColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Component.translatable("speedrunigt.option.timer_position.color_blue", "IGT").append(" : ").append(String.valueOf(ARGB.blue(drawer.getIGTColor()))));
                    }

                    @Override
                    protected void applyValue() {
                        int color = drawer.getIGTColor();
                        drawer.setIGTColor(
                                ARGB.color(
                                        ARGB.alpha(color),
                                        ARGB.red(color),
                                        ARGB.green(color),
                                        (int) (this.value * 255)
                                )
                        );
                        changed = true;
                    }
                })
        );

        igtOptions.add(
                addRenderableWidget(new AbstractSliderButton(width / 2 + 6, height / 2 - 16, 120, 20, Component.translatable("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append((Math.round(drawer.getIGTScale() * 100)) + "%"), drawer.getIGTScale() / 3f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Component.translatable("speedrunigt.option.timer_position.scale", "IGT").append(" : ").append((Math.round(drawer.getIGTScale() * 100)) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setIGTScale(Math.round((float) this.value * 3f * 20f)/20f);
                        changed = true;
                    }
                })
        );

        igtOptions.add(
                addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 6, height / 2 + 6, 120, 20, Component.translatable("speedrunigt.option.timer_position.text_decorate", "IGT").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))), (Button button) -> {
                    int order = drawer.getIGTDecoration().ordinal();
                    drawer.setIGTDecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    changed = true;
                    button.setMessage(Component.translatable("speedrunigt.option.timer_position.text_decorate", "IGT").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.text_decorate." + drawer.getIGTDecoration().name().toLowerCase(Locale.ROOT))));
                }))
        );

        igtOptions.add(
                addRenderableWidget(ButtonWidgetHelper.create(this.width / 2 + 6, this.height / 2 + 28, 120, 20, Component.translatable("speedrunigt.option.timer_position.align", "IGT").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.align." + drawer.getIGTDisplayAlign().name().toLowerCase(Locale.ROOT))), (Button button) -> {
                    int order = drawer.getIGTDisplayAlign().ordinal();
                    drawer.setIGTDisplayAlign(TimerDisplayAlign.values()[(++order) % TimerDisplayAlign.values().length]);
                    changed = true;
                    button.setMessage(Component.translatable("speedrunigt.option.timer_position.align", "IGT").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.align." + drawer.getIGTDisplayAlign().name().toLowerCase(Locale.ROOT))));
                }))
        );
    }

    public void initRTAButtons() {
        rtaOptions.add(
                addRenderableWidget(new AbstractSliderButton(width / 2 - 127, height / 2 - 16, 120, 20, Component.translatable("speedrunigt.option.timer_position.color_red", "RTA").append(" : ").append(String.valueOf(ARGB.red(drawer.getRTAColor()))), ARGB.red(drawer.getRTAColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Component.translatable("speedrunigt.option.timer_position.color_red", "RTA").append(" : ").append(String.valueOf(ARGB.red(drawer.getRTAColor()))));
                    }

                    @Override
                    protected void applyValue() {
                        int color = drawer.getRTAColor();
                        drawer.setRTAColor(
                                ARGB.color(
                                        ARGB.alpha(color),
                                        (int) (this.value * 255),
                                        ARGB.green(color),
                                        ARGB.blue(color)
                                )
                        );
                        changed = true;
                    }
                })
        );

        rtaOptions.add(
                addRenderableWidget(new AbstractSliderButton(width / 2 - 127, height / 2 + 6, 120, 20, Component.translatable("speedrunigt.option.timer_position.color_green", "RTA").append(" : ").append(String.valueOf(ARGB.green(drawer.getRTAColor()))), ARGB.green(drawer.getRTAColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Component.translatable("speedrunigt.option.timer_position.color_green", "RTA").append(" : ").append(String.valueOf(ARGB.green(drawer.getRTAColor()))));
                    }

                    @Override
                    protected void applyValue() {
                        int color = drawer.getRTAColor();
                        drawer.setRTAColor(
                                ARGB.color(
                                        ARGB.alpha(color),
                                        ARGB.red(color),
                                        (int) (this.value * 255),
                                        ARGB.blue(color)
                                )
                        );
                        changed = true;
                    }
                })
        );

        rtaOptions.add(
                addRenderableWidget(new AbstractSliderButton(width / 2 - 127, height / 2 + 28, 120, 20, Component.translatable("speedrunigt.option.timer_position.color_blue", "RTA").append(" : ").append(String.valueOf(ARGB.blue(drawer.getRTAColor()))), ARGB.blue(drawer.getRTAColor()) / 255.0f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Component.translatable("speedrunigt.option.timer_position.color_blue", "RTA").append(" : ").append(String.valueOf(ARGB.blue(drawer.getRTAColor()))));
                    }

                    @Override
                    protected void applyValue() {
                        int color = drawer.getRTAColor();
                        drawer.setRTAColor(
                                ARGB.color(
                                        ARGB.alpha(color),
                                        ARGB.red(color),
                                        ARGB.green(color),
                                        (int) (this.value * 255)
                                )
                        );
                        changed = true;
                    }
                })
        );

        rtaOptions.add(
                addRenderableWidget(new AbstractSliderButton(width / 2 + 6, height / 2 - 16, 120, 20, Component.translatable("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append((Math.round(drawer.getRTAScale() * 100)) + "%"), drawer.getRTAScale() / 3f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Component.translatable("speedrunigt.option.timer_position.scale", "RTA").append(" : ").append((Math.round(drawer.getRTAScale() * 100)) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setRTAScale(Math.round((float) this.value * 3f * 20f)/20f);
                        changed = true;
                    }
                })
        );

        rtaOptions.add(
                addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 6, height / 2 + 6, 120, 20, Component.translatable("speedrunigt.option.timer_position.text_decorate", "RTA").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.text_decorate." + drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))), (Button button) -> {
                    int order = drawer.getRTADecoration().ordinal();
                    drawer.setRTADecoration(TimerDecoration.values()[(++order) % TimerDecoration.values().length]);
                    changed = true;
                    button.setMessage(Component.translatable("speedrunigt.option.timer_position.text_decorate", "RTA").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.text_decorate." + drawer.getRTADecoration().name().toLowerCase(Locale.ROOT))));
                }))
        );

        rtaOptions.add(
                addRenderableWidget(ButtonWidgetHelper.create(this.width / 2 + 6, this.height / 2 + 28, 120, 20, Component.translatable("speedrunigt.option.timer_position.align", "RTA").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.align." + drawer.getRTADisplayAlign().name().toLowerCase(Locale.ROOT))), (Button button) -> {
                    int order = drawer.getRTADisplayAlign().ordinal();
                    drawer.setRTADisplayAlign(TimerDisplayAlign.values()[(++order) % TimerDisplayAlign.values().length]);
                    changed = true;
                    button.setMessage(Component.translatable("speedrunigt.option.timer_position.align", "RTA").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.align." + drawer.getRTADisplayAlign().name().toLowerCase(Locale.ROOT))));
                }))
        );
    }

    public void initPositionButtons() {
        Button posTypeButton = addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 80, height / 2 + 6, 160, 20, Component.translatable("speedrunigt.option.timer_position.split_position_type").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.split_position_type."+currentPosType.name().toLowerCase(Locale.ROOT))), (button) -> {
            int order = (currentPosType.ordinal() + 1) % PositionType.values().length;
            currentPosType = PositionType.values()[order];
            changed = true;
            refreshPosition();
            button.setMessage(Component.translatable("speedrunigt.option.timer_position.split_position_type").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.split_position_type."+currentPosType.name().toLowerCase(Locale.ROOT))));
        }));
        posTypeButton.active = splitPosition;

        posOptions.add(
                addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 80, height / 2 - 16, 160, 20, Component.translatable("speedrunigt.option.timer_position.split_position").append(" : ").append(splitPosition ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF), (button) -> {
                    splitPosition = !splitPosition;
                    changed = true;
                    posTypeButton.active = splitPosition;
                    button.setMessage(Component.translatable("speedrunigt.option.timer_position.split_position").append(" : ").append(splitPosition ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF));
                    if (!splitPosition) {
                        currentPosType = PositionType.DEFAULT;
                        refreshPosition();
                        posTypeButton.setMessage(Component.translatable("speedrunigt.option.timer_position.split_position_type").append(" : ").append(Component.translatable("speedrunigt.option.timer_position.split_position_type."+currentPosType.name().toLowerCase(Locale.ROOT))));
                    }
                }))
        );

        posOptions.add(posTypeButton);
    }

    public void initFontButtons() {
        Button prevButton = addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 180, height / 2 + 6, 20, 20, Component.literal("<"), (Button button) -> {
            fontPage--;
            openFontPage();
        }));

        Button nextButton = addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 180, height / 2 + 6, 20, 20, Component.literal(">"), (Button button) -> {
            fontPage++;
            openFontPage();
        }));
        fontOptions.add(prevButton);
        fontOptions.add(nextButton);

        fontSelectButtons.add(
                addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 35, height / 2 - 16, 50, 20, Component.translatable("speedrunigt.option.select"), (Button button) -> {
                    int c = (fontPage * 3);
                    if (availableFonts.size() > c) {
                        for (Button fontSelectButton : fontSelectButtons) fontSelectButton.active = true;
                        drawer.setTimerFont(availableFonts.get(c));
                        button.active = false;
                        openFontPage();
                        changed = true;
                    }
                }))
        );
        fontSelectButtons.add(
                addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 35, height / 2 + 6, 50, 20, Component.translatable("speedrunigt.option.select"), (Button button) -> {
                    int c = (fontPage * 3) + 1;
                    if (availableFonts.size() > c) {
                        for (Button fontSelectButton : fontSelectButtons) fontSelectButton.active = true;
                        drawer.setTimerFont(availableFonts.get(c));
                        button.active = false;
                        openFontPage();
                        changed = true;
                    }
                }))
        );
        fontSelectButtons.add(
                addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 35, height / 2 + 28, 50, 20, Component.translatable("speedrunigt.option.select"), (Button button) -> {
                    int c = (fontPage * 3) + 2;
                    if (availableFonts.size() > c) {
                        for (Button fontSelectButton : fontSelectButtons) fontSelectButton.active = true;
                        drawer.setTimerFont(availableFonts.get(c));
                        button.active = false;
                        openFontPage();
                        changed = true;
                    }
                }))
        );
        for (AbstractWidget fontOption : fontOptions) {
            fontOption.visible = false;
        }
        for (Button fontSelectButton : fontSelectButtons) {
            fontSelectButton.visible = false;
        }
        fontOptions.addAll(fontSelectButtons);


        fontConfigButton = addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 88, 0, 50, 20, Component.literal("Config"), (Button button) -> {
            if (minecraft != null) minecraft.gui.setScreen(new FontConfigScreen(this, drawer.getTimerFont()));
        } ));
        fontOptions.add(addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 154, height / 2 - 80, 150, 20, Component.translatable("speedrunigt.option.timer_position.font.open_folder"), (Button button) -> Util.getPlatform().openFile(SpeedRunIGT.FONT_PATH.toFile()))));
        fontOptions.add(addRenderableWidget(ButtonWidgetHelper.create(width / 2 + 4, height / 2 - 80, 150, 20, Component.translatable("speedrunigt.option.timer_position.font.description"), (Button button) -> Util.getPlatform().openUri("https://youtu.be/agBbiTQWj78"))));
        openFontPage();
    }

    public void openFontPage() {
        fontOptions.get(0).active = fontPage != 0;
        fontOptions.get(1).active = fontPage != Math.max((availableFonts.size() - 1) / 3, 0);

        int c = fontPage * 3;
        int available = 0;
        for (int i = 0; i < fontSelectButtons.size(); i++) {
            Button button = fontSelectButtons.get(i);
            if (c + i < availableFonts.size()) {
                button.active = !availableFonts.get(c + i).toString().equals(drawer.getTimerFont().toString());
                if (!button.active && Objects.equals(drawer.getTimerFont().getNamespace(), SpeedRunIGT.MOD_ID)) available = button.getY();
                button.visible = true;
            } else {
                button.visible = false;
            }
        }

        fontConfigButton.visible = currentTab == 3 && available != 0;
        fontConfigButton.setY(available);
    }

    public void initBackgroundButtons() {
        backgroundOptions.add(
                addRenderableWidget(new AbstractSliderButton(width / 2 - 80, height / 2 - 16, 160, 20, Component.translatable("speedrunigt.option.timer_position.background_opacity").append(" : ").append((int) (drawer.getBGOpacity()*100) + "%"), drawer.getBGOpacity()) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Component.translatable("speedrunigt.option.timer_position.background_opacity").append(" : ").append((int) (drawer.getBGOpacity()*100) + "%"));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setBGOpacity((float) this.value);
                        changed = true;
                    }
                })
        );

        backgroundOptions.add(
                addRenderableWidget(new AbstractSliderButton(width / 2 - 80, height / 2 + 6, 160, 20, Component.translatable("speedrunigt.option.timer_position.background_padding", "RTA").append(" : ").append(String.valueOf(drawer.getRTAPadding())), (drawer.getRTAPadding()-1) / 24f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Component.translatable("speedrunigt.option.timer_position.background_padding", "RTA").append(" : ").append(String.valueOf(drawer.getRTAPadding())));
                    }

                    @Override
                    protected void applyValue() {
                        drawer.setRTAPadding((int) (this.value * 24) + 1);
                        changed = true;
                    }
                })
        );

        backgroundOptions.add(
                addRenderableWidget(new AbstractSliderButton(width / 2 - 80, height / 2 + 28, 160, 20, Component.translatable("speedrunigt.option.timer_position.background_padding", "IGT").append(" : ").append(String.valueOf(drawer.getIGTPadding())), (drawer.getIGTPadding()-1) / 24f) {
                    @Override
                    protected void updateMessage() {
                        this.setMessage(Component.translatable("speedrunigt.option.timer_position.background_padding", "IGT").append(" : ").append(String.valueOf(drawer.getIGTPadding())));
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
