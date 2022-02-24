package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerRecord;
import com.redlimerl.speedrunigt.timer.running.RunSplitType;
import com.redlimerl.speedrunigt.timer.running.RunSplitTypes;
import com.redlimerl.speedrunigt.timer.running.RunType;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class TimerSplitListScreen extends Screen {

    private final Screen parent;
    private TimerSplitListWidget listWidget;
    private RunType filter = null;
    private enum RunOrder {
        FASTEST, SLOWEST, NEWEST, OLDEST;
        public TranslatableText getText() { return new TranslatableText("speedrunigt.split.order." + this.name().toLowerCase(Locale.ROOT)); }
    }
    private RunOrder runOrder = RunOrder.FASTEST;

    public TimerSplitListScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.split.title"));
        this.parent = parent;
    }

    private BaseText getFilterText() {
        return filter == null ? new TranslatableText("gui.all") : new LiteralText(filter.getContext());
    }

    @Override
    protected void init() {
        this.listWidget = new TimerSplitListWidget();
        children.add(listWidget);

        addButton(new ButtonWidget(10, 6, 120, 20, new TranslatableText("speedrunigt.option.show").append(" : ").append(this.getFilterText()).asFormattedString(), (ButtonWidget button) -> {
            int order = filter == null ? -1 : filter.ordinal();
            if (order + 1 == RunType.values().length) {
                filter = null;
            } else {
                filter = RunType.values()[(++order) % RunType.values().length];
            }
            this.listWidget.applyFilter(filter, runOrder);
            button.setMessage(new TranslatableText("speedrunigt.option.show").append(" : ").append(this.getFilterText()).asFormattedString());
        }));

        addButton(new ButtonWidget(134, 6, 120, 20, new TranslatableText("speedrunigt.split.order").append(" : ").append(this.runOrder.getText()).asFormattedString(), (ButtonWidget button) -> {
            int order = runOrder.ordinal() + 1;
            runOrder = RunOrder.values()[order % RunOrder.values().length];
            this.listWidget.applyFilter(filter, runOrder);
            button.setMessage(new TranslatableText("speedrunigt.split.order").append(" : ").append(this.runOrder.getText()).asFormattedString());
        }));

//        addButton(new ButtonWidget(width/2 - 153, height - 32, 150, 20, new TranslatableText("speedrunigt.option.split_notification").append(" : ").append(new TranslatableText("speedrunigt.option.split_notification." + SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE).name().toLowerCase(Locale.ROOT))), (ButtonWidget button) -> {
//            int order = SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE).ordinal();
//            SpeedRunOptions.setOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE, SpeedRunOptions.SplitDisplayType.values()[(++order) % SpeedRunOptions.SplitDisplayType.values().length]);
//            button.setMessage(new TranslatableText("speedrunigt.option.split_notification").append(" : ").append(new TranslatableText("speedrunigt.option.split_notification." + SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE).name().toLowerCase(Locale.ROOT))));
//        }));

        addButton(new ButtonWidget(width/2 - 75, height - 32, 150, 20, ScreenTexts.DONE, (ButtonWidget button) -> onClose()));
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        renderBackground();
        listWidget.render(mouseX, mouseY, delta);
        if (listWidget.children().size() == 0)
            drawCenteredString(font, "▽◠▽.. :(", width / 2, height / 2, 16777215);
        super.render(mouseX, mouseY, delta);

        if (listWidget.tooltip != null) {
            renderTooltip(listWidget.tooltip, mouseX, mouseY);
            listWidget.tooltip = null;
        }
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.openScreen(parent);
    }

    private class TimerSplitListWidget extends ElementListWidget<TimerSplitListWidget.TimerSplitEntry> {
        private List<String> tooltip = null;

        public TimerSplitListWidget() {
            super(TimerSplitListScreen.this.minecraft,
                    TimerSplitListScreen.this.width, TimerSplitListScreen.this.height,
                    32, TimerSplitListScreen.this.height - 42, 24);
            this.applyFilter(TimerSplitListScreen.this.filter, TimerSplitListScreen.this.runOrder);
        }

        void applyFilter(RunType filter, RunOrder runOrder) {
            ArrayList<TimerRecord> splitList = new ArrayList<>();
            for (TimerRecord split : TimerRecord.RECORD_LIST) {
                if (filter == null || split.getRunType() == filter) splitList.add(split);
            }
            splitList.sort((o1, o2) -> {
                switch (runOrder) {
                    case FASTEST:
                        return Long.compare(o1.getResultTime(), o2.getResultTime());
                    case SLOWEST:
                        return Long.compare(o2.getResultTime(), o1.getResultTime());
                    case OLDEST:
                        return Long.compare(o1.getTimestamp(), o2.getTimestamp());
                    case NEWEST:
                        return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                    default:
                        return 0;
                }
            });

            this.replaceEntries(splitList.stream().map(TimerSplitEntry::new).collect(Collectors.toList()));
            setScrollAmount(0);
        }

        @Override
        public void render(int mouseX, int mouseY, float delta) {
            super.render(mouseX, mouseY, delta);
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 100;
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 50;
        }

        private class TimerSplitEntry extends ElementListWidget.Entry<TimerSplitEntry> {
            private final ArrayList<AbstractButtonWidget> children = new ArrayList<>();
            private final Text titleText;
            private BaseText resultTimeText = new LiteralText("");
            private final ArrayList<String> timelineTextList = new ArrayList<>();
            private final ButtonWidget deleteButton;
            private final long time;
            private final TimerRecord split;
            private final long entryCreatedTime = System.currentTimeMillis();

            private TimerSplitEntry(TimerRecord split) {
                this.split = split;
                this.time = split.getTimestamp();
                this.titleText = new LiteralText(String.format("%s - %s %s%s",
                        split.getVersion(), (split.isCoop() ? "Co-op " : "") + split.getRunCategory().getText().getString(), split.getRunType().getContext(),
                        (split.getRunType() == RunType.SET_SEED || split.getRunType() == RunType.SAVED_WORLD) ? (" [" + split.getSeed() + "]") : ""))
                        .formatted(Formatting.GRAY);
                for (Map.Entry<String, Long> splitPoint : split.getSplitTimeline().entrySet()) {
                    RunSplitType splitType = RunSplitType.getSplitType(splitPoint.getKey());
                    String time = InGameTimer.timeToStringFormat(splitPoint.getValue());
                    if (splitType == RunSplitTypes.COMPLETE) {
                        resultTimeText = new LiteralText("§a" + I18n.translate("speedrunigt.split.run_end") + ": §f" + time);
                    } else {
                        timelineTextList.add(new LiteralText("§b" + I18n.translate(splitType.getTranslateKey()) + ": §f" + time).asFormattedString());
                    }
                }
                this.deleteButton = new ButtonWidget(0, 0, 40, 20, new TranslatableText("selectWorld.delete").getString(), buttonWidget -> {
                    if (Objects.equals(buttonWidget.getMessage(), new TranslatableText("selectWorld.delete").getString())) {
                        buttonWidget.setMessage(new TranslatableText("selectWorld.delete").append("?").getString());
                    } else {
                        split.delete();
                        TimerSplitListWidget.this.removeEntry(this);
                    }
                });
                children.add(this.deleteButton);
            }

            @Override
            public List<? extends Element> children() {
                return children;
            }

            @Override
            public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                this.deleteButton.x = x + entryWidth - this.deleteButton.getWidth();
                this.deleteButton.y = y + 2;
                this.deleteButton.render(mouseX, mouseY, tickDelta);

                drawString(font, titleText.asFormattedString(), x, y + 4, 16777215);
                if (timelineTextList.size() > 0) {
                    int count = (int) ((System.currentTimeMillis() - entryCreatedTime) / 2500) % timelineTextList.size();
                    drawString(font, resultTimeText.deepCopy().append("  ").append(timelineTextList.get(count)).asFormattedString(), x, y + 13, 16777215);
                } else {
                    drawString(font, resultTimeText.asFormattedString(), x, y + 13, 16777215);
                }
                drawCenteredString(font, "§8§m                                                                                ",
                        x + (entryWidth / 2), y + 20, 16777215);

                ArrayList<String> tooltip = new ArrayList<>();
                if (this.deleteButton.isMouseOver(mouseX, mouseY) && !Objects.equals(this.deleteButton.getMessage(), I18n.translate("selectWorld.delete"))) {
                    tooltip.add(new TranslatableText("speedrunigt.message.click_delete_button_again").asFormattedString());
                    TimerSplitListWidget.this.tooltip = tooltip;
                } else if (isMouseOver(mouseX, mouseY)) {
                    tooltip.add(titleText.deepCopy().formatted(Formatting.YELLOW).asFormattedString());
                    tooltip.add(new LiteralText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time))).formatted(Formatting.GRAY).asFormattedString());
                    if (split.getRunType() != RunType.SET_SEED && split.getRunType() != RunType.SAVED_WORLD)
                        tooltip.add(new LiteralText(I18n.translate("commands.seed.success", split.getSeed())).formatted(Formatting.GRAY).asFormattedString());
                    tooltip.add("");
                    if (timelineTextList.size() > 0) {
                        tooltip.addAll(timelineTextList);
                        tooltip.add("");
                    }
                    tooltip.add(resultTimeText.asFormattedString());
                    TimerSplitListWidget.this.tooltip = tooltip;
                }
            }
        }
    }
}
