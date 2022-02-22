package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerRecord;
import com.redlimerl.speedrunigt.timer.running.RunSplitType;
import com.redlimerl.speedrunigt.timer.running.RunSplitTypes;
import com.redlimerl.speedrunigt.timer.running.RunType;
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

    private MutableText getFilterText() {
        return filter == null ? new TranslatableText("gui.all") : new LiteralText(filter.getContext());
    }

    @Override
    protected void init() {
        this.listWidget = new TimerSplitListWidget();
        addChild(listWidget);

        addButton(new ButtonWidget(10, 6, 120, 20, new TranslatableText("speedrunigt.option.show").append(" : ").append(this.getFilterText()), (ButtonWidget button) -> {
            int order = filter == null ? -1 : filter.ordinal();
            if (order + 1 == RunType.values().length) {
                filter = null;
            } else {
                filter = RunType.values()[(++order) % RunType.values().length];
            }
            this.listWidget.applyFilter(filter, runOrder);
            button.setMessage(new TranslatableText("speedrunigt.option.show").append(" : ").append(this.getFilterText()));
        }));

        addButton(new ButtonWidget(134, 6, 120, 20, new TranslatableText("speedrunigt.split.order").append(" : ").append(this.runOrder.getText()), (ButtonWidget button) -> {
            int order = runOrder.ordinal() + 1;
            runOrder = RunOrder.values()[order % RunOrder.values().length];
            this.listWidget.applyFilter(filter, runOrder);
            button.setMessage(new TranslatableText("speedrunigt.split.order").append(" : ").append(this.runOrder.getText()));
        }));

//        addButton(new ButtonWidget(width/2 - 153, height - 32, 150, 20, new TranslatableText("speedrunigt.option.split_notification").append(" : ").append(new TranslatableText("speedrunigt.option.split_notification." + SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE).name().toLowerCase(Locale.ROOT))), (ButtonWidget button) -> {
//            int order = SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE).ordinal();
//            SpeedRunOptions.setOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE, SpeedRunOptions.SplitDisplayType.values()[(++order) % SpeedRunOptions.SplitDisplayType.values().length]);
//            button.setMessage(new TranslatableText("speedrunigt.option.split_notification").append(" : ").append(new TranslatableText("speedrunigt.option.split_notification." + SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE).name().toLowerCase(Locale.ROOT))));
//        }));

        addButton(new ButtonWidget(width/2 - 75, height - 32, 150, 20, ScreenTexts.DONE, (ButtonWidget button) -> onClose()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        listWidget.render(matrices, mouseX, mouseY, delta);
        if (listWidget.children().size() == 0)
            drawCenteredString(matrices, textRenderer, "▽◠▽.. :(", width / 2, height / 2, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (client != null) client.openScreen(parent);
    }

    private class TimerSplitListWidget extends ElementListWidget<TimerSplitListWidget.TimerSplitEntry> {
        private List<MutableText> tooltip = null;

        public TimerSplitListWidget() {
            super(TimerSplitListScreen.this.client,
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
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
            if (tooltip != null) {
                renderTooltip(matrices, tooltip, mouseX, mouseY);
                tooltip = null;
            }
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
            private final MutableText titleText;
            private MutableText resultTimeText = new LiteralText("");
            private final ArrayList<MutableText> timelineTextList = new ArrayList<>();
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
                        timelineTextList.add(new LiteralText("§b" + I18n.translate(splitType.getTranslateKey()) + ": §f" + time));
                    }
                }
                this.deleteButton = new ButtonWidget(0, 0, 40, 20, new TranslatableText("selectWorld.delete"), buttonWidget -> {
                    if (Objects.equals(buttonWidget.getMessage().getString(), new TranslatableText("selectWorld.delete").getString())) {
                        buttonWidget.setMessage(new TranslatableText("selectWorld.delete").append("?"));
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
            public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                this.deleteButton.x = x + entryWidth - this.deleteButton.getWidth();
                this.deleteButton.y = y + 2;
                this.deleteButton.render(matrices, mouseX, mouseY, tickDelta);

                drawTextWithShadow(matrices, textRenderer, titleText, x, y + 4, 16777215);
                if (timelineTextList.size() > 0) {
                    int count = (int) ((System.currentTimeMillis() - entryCreatedTime) / 2500) % timelineTextList.size();
                    drawTextWithShadow(matrices, textRenderer, resultTimeText.shallowCopy().append("  ").append(timelineTextList.get(count)), x, y + 13, 16777215);
                } else {
                    drawTextWithShadow(matrices, textRenderer, resultTimeText, x, y + 13, 16777215);
                }
                drawCenteredString(matrices, textRenderer, "§8§m                                                                                ",
                        x + (entryWidth / 2), y + 20, 16777215);

                ArrayList<MutableText> tooltip = new ArrayList<>();
                if (this.deleteButton.isMouseOver(mouseX, mouseY) && !Objects.equals(this.deleteButton.getMessage().getString(), I18n.translate("selectWorld.delete"))) {
                    tooltip.add(new TranslatableText("speedrunigt.message.click_delete_button_again"));
                    TimerSplitListWidget.this.tooltip = tooltip;
                } else if (isMouseOver(mouseX, mouseY)) {
                    tooltip.add(titleText.shallowCopy().formatted(Formatting.YELLOW));
                    tooltip.add(new LiteralText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time))).formatted(Formatting.GRAY));
                    if (split.getRunType() != RunType.SET_SEED && split.getRunType() != RunType.SAVED_WORLD)
                        tooltip.add(new LiteralText(I18n.translate("commands.seed.success", split.getSeed())).formatted(Formatting.GRAY));
                    tooltip.add(new LiteralText(""));
                    if (timelineTextList.size() > 0) {
                        tooltip.addAll(timelineTextList);
                        tooltip.add(new LiteralText(""));
                    }
                    tooltip.add(resultTimeText);
                    TimerSplitListWidget.this.tooltip = tooltip;
                }
            }
        }
    }
}
