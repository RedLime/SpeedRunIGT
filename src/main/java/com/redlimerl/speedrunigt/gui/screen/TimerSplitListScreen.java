package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.timer.RunType;
import com.redlimerl.speedrunigt.timer.TimerSplit;
import com.redlimerl.speedrunigt.timer.TimerSplit.SplitType;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class TimerSplitListScreen extends Screen {

    private final Screen parent;
    private TimerSplitListWidget listWidget;
    private RunType filter = null;

    public TimerSplitListScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.split.title"));
        this.parent = parent;
    }

    private MutableText getFilterText() {
        return filter == null ? new TranslatableText("gui.all") : new LiteralText(filter.name());
    }

    @Override
    protected void init() {
        this.listWidget = new TimerSplitListWidget();
        addChild(listWidget);

        addButton(new ButtonWidget(10, 6, 80, 20, new TranslatableText("speedrunigt.split.filter").append(" : ").append(this.getFilterText()), (ButtonWidget button) -> {
            int order = filter == null ? -1 : filter.ordinal();
            if (order + 1 == RunType.values().length) {
                filter = null;
            } else {
                filter = RunType.values()[(++order) % RunType.values().length];
            }
            this.listWidget.applyFilter(filter);
            button.setMessage(new TranslatableText("speedrunigt.split.filter").append(" : ").append(this.getFilterText()));
        }));

        addButton(new ButtonWidget(width/2 - 153, height - 32, 150, 20, new TranslatableText("speedrunigt.option.split_notification").append(" : ").append(new TranslatableText("speedrunigt.option.split_notification." + SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE).name().toLowerCase(Locale.ROOT))), (ButtonWidget button) -> {
            int order = SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE).ordinal();
            SpeedRunOptions.setOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE, SpeedRunOptions.SplitDisplayType.values()[(++order) % SpeedRunOptions.SplitDisplayType.values().length]);
            button.setMessage(new TranslatableText("speedrunigt.option.split_notification").append(" : ").append(new TranslatableText("speedrunigt.option.split_notification." + SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE).name().toLowerCase(Locale.ROOT))));
        }));

        addButton(new ButtonWidget(width/2 + 3, height - 32, 150, 20, ScreenTexts.DONE, (ButtonWidget button) -> onClose()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        listWidget.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, textRenderer, title, width / 2, 12, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (client != null) client.openScreen(parent);
    }

    private class TimerSplitListWidget extends ElementListWidget<TimerSplitListWidget.TimerSplitEntry> {
        public TimerSplitListWidget() {
            super(TimerSplitListScreen.this.client,
                    TimerSplitListScreen.this.width, TimerSplitListScreen.this.height,
                    32, TimerSplitListScreen.this.height - 42,
                    24);
            this.applyFilter(TimerSplitListScreen.this.filter);
        }

        void applyFilter(RunType filter) {
            if (filter == null) {
                this.replaceEntries(TimerSplit.SPLIT_DATA.entrySet().stream().map(entry -> new TimerSplitEntry(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
            } else if (filter == RunType.RSG) {
                this.replaceEntries(TimerSplit.SPLIT_DATA.entrySet().stream().filter(entry -> Objects.equals(entry.getKey().split(":")[0], RunType.RSG.name())).map(entry -> new TimerSplitEntry(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
            } else if (filter == RunType.FSG) {
                this.replaceEntries(TimerSplit.SPLIT_DATA.entrySet().stream().filter(entry -> Objects.equals(entry.getKey().split(":")[0], RunType.FSG.name())).map(entry -> new TimerSplitEntry(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
            } else if (filter == RunType.SSG) {
                this.replaceEntries(TimerSplit.SPLIT_DATA.entrySet().stream().filter(entry -> !(Objects.equals(entry.getKey().split(":")[0], RunType.RSG.name()) || Objects.equals(entry.getKey().split(":")[0], RunType.FSG.name()))).map(entry -> new TimerSplitEntry(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
            }
            setScrollAmount(0);
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 100;
        }

        @Override
        protected int getScrollbarPositionX() {
            return super.getScrollbarPositionX() + 50;
        }

        private class TimerSplitEntry extends ElementListWidget.Entry<TimerSplitEntry> {
            private final ArrayList<AbstractButtonWidget> children = new ArrayList<>();
            private final String timeKey;
            private final MutableText timeKeyInfo;
            private MutableText completeTimeValue = new LiteralText("");
            private final ArrayList<MutableText> timeValues = new ArrayList<>();
            private final ButtonWidget deleteButton;
            private final long entryCreatedTime = System.currentTimeMillis();

            private TimerSplitEntry(String timeKey, String timeValue) {
                this.timeKey = timeKey;
                this.timeKeyInfo = new LiteralText(
                        timeKey.split(":")[2]
                        + " - " + RunCategory.valueOf(timeKey.split(":")[1]).getText().getString()
                        + " / " + (Objects.equals(timeKey.split(":")[0], "-") ? timeKey.split(":")[0] : ("Seed: " + timeKey.split(":")[0]))
                ).formatted(Formatting.GRAY);
                for (String tv : timeValue.split(",")) {
                    SplitType splitType = SplitType.valueOf(tv.split("\\|")[0]);
                    String time = InGameTimer.timeToStringFormat(Long.parseLong(tv.split("\\|")[1]));
                    if (splitType == SplitType.COMPLETE) {
                        completeTimeValue = new LiteralText("§a" + I18n.translate("speedrunigt.split.run_end") + ": §f" + time);
                    } else {
                        timeValues.add(new LiteralText("§b" + I18n.translate(splitType.getTitleKey()) + ": §f" + time));
                    }
                }
                this.deleteButton = new ButtonWidget(0, 0, 40, 20, new TranslatableText("selectWorld.delete"), buttonWidget -> {
                    if (Objects.equals(buttonWidget.getMessage().getString(), new TranslatableText("selectWorld.delete").getString())) {
                        buttonWidget.setMessage(new TranslatableText("selectWorld.delete").append("?"));
                    } else {
                        TimerSplit.SPLIT_DATA.remove(this.timeKey);
                        TimerSplitListWidget.this.removeEntry(this);
                        TimerSplit.save();
                    }
                });
                children.add(this.deleteButton);
            }

            @Override
            public List<? extends Element> children() {
                return children;
            }

            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                this.deleteButton.x = x + entryWidth - this.deleteButton.getWidth();
                this.deleteButton.y = y + 2;
                this.deleteButton.render(matrices, mouseX, mouseY, tickDelta);

                drawTextWithShadow(matrices, textRenderer, timeKeyInfo, x, y + 4, 16777215);
                if (timeValues.size() > 0) {
                    int count = (int) ((System.currentTimeMillis() - entryCreatedTime) / 2500) % timeValues.size();
                    drawTextWithShadow(matrices, textRenderer, completeTimeValue.shallowCopy().append("  ").append(timeValues.get(count)), x, y + 13, 16777215);
                } else {
                    drawTextWithShadow(matrices, textRenderer, completeTimeValue, x, y + 13, 16777215);
                }
                drawCenteredString(matrices, textRenderer, "§8§m                                                                                ",
                        x + (entryWidth / 2), y + 20, 16777215);

                if (this.deleteButton.isMouseOver(mouseX, mouseY) && !Objects.equals(this.deleteButton.getMessage().getString(), I18n.translate("selectWorld.delete"))) {
                    renderTooltip(matrices, new TranslatableText("speedrunigt.message.click_delete_button_again"), mouseX, mouseY);
                } else if (isMouseOver(mouseX, mouseY)) {
                    ArrayList<MutableText> tooltip = new ArrayList<>(timeValues);
                    tooltip.add(0, timeKeyInfo.shallowCopy().formatted(Formatting.YELLOW));
                    tooltip.add(new LiteralText(""));
                    tooltip.add(completeTimeValue);
                    renderTooltip(matrices, tooltip, mouseX, mouseY);
                }
            }
        }
    }
}
