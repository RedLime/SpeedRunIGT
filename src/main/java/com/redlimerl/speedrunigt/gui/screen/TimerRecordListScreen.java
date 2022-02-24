package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerRecord;
import com.redlimerl.speedrunigt.timer.running.RunSplitType;
import com.redlimerl.speedrunigt.timer.running.RunSplitTypes;
import com.redlimerl.speedrunigt.timer.running.RunType;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.text.SimpleDateFormat;
import java.util.*;

public class TimerRecordListScreen extends Screen {

    private final Screen parent;
    private TimerSplitListWidget listWidget;
    private final ArrayList<ButtonWidget> widgetButtons = new ArrayList<>();
    private RunType filter = null;
    private enum RunOrder {
        FASTEST, SLOWEST, NEWEST, OLDEST;
        public TranslatableText getText() { return new TranslatableText("speedrunigt.split.order." + this.name().toLowerCase(Locale.ROOT)); }
    }
    private RunOrder runOrder = RunOrder.FASTEST;

    public TimerRecordListScreen(Screen parent) {
        this.parent = parent;
    }

    private BaseText getFilterText() {
        return filter == null ? new TranslatableText("gui.all") : new LiteralText(filter.getContext());
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick(this);
        }
        super.buttonClicked(button);
    }

    @Override
    public void handleMouse() {
        super.handleMouse();
        this.listWidget.handleMouse();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        ArrayList<ButtonWidget> widgets = new ArrayList<>(widgetButtons);
        buttons.addAll(widgets);
        super.mouseClicked(mouseX, mouseY, button);
        buttons.removeAll(widgets);
    }

    public <T extends ButtonWidget> void addButton(T button) {
        buttons.add(button);
    }

    @Override
    public void init() {
        this.listWidget = new TimerSplitListWidget();

        addButton(new ConsumerButtonWidget(10, 6, 120, 20, new TranslatableText("speedrunigt.option.show").append(" : ").append(this.getFilterText()).asFormattedString(), (screen, button) -> {
            int order = filter == null ? -1 : filter.ordinal();
            if (order + 1 == RunType.values().length) {
                filter = null;
            } else {
                filter = RunType.values()[(++order) % RunType.values().length];
            }
            this.listWidget.applyFilter(filter, runOrder);
            button.message = (new TranslatableText("speedrunigt.option.show").append(" : ").append(this.getFilterText()).asFormattedString());
        }));

        addButton(new ConsumerButtonWidget(134, 6, 120, 20, new TranslatableText("speedrunigt.split.order").append(" : ").append(this.runOrder.getText()).asFormattedString(), (screen, button) -> {
            int order = runOrder.ordinal() + 1;
            runOrder = RunOrder.values()[order % RunOrder.values().length];
            this.listWidget.applyFilter(filter, runOrder);
            button.message = (new TranslatableText("speedrunigt.split.order").append(" : ").append(this.runOrder.getText()).asFormattedString());
        }));

//        addButton(new ButtonWidget(width/2 - 153, height - 32, 150, 20, new TranslatableText("speedrunigt.option.split_notification").append(" : ").append(new TranslatableText("speedrunigt.option.split_notification." + SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE).name().toLowerCase(Locale.ROOT))), (ButtonWidget button) -> {
//            int order = SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE).ordinal();
//            SpeedRunOptions.setOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE, SpeedRunOptions.SplitDisplayType.values()[(++order) % SpeedRunOptions.SplitDisplayType.values().length]);
//            button.setMessage(new TranslatableText("speedrunigt.option.split_notification").append(" : ").append(new TranslatableText("speedrunigt.option.split_notification." + SpeedRunOptions.getOption(SpeedRunOptions.SPLIT_DISPLAY_TYPE).name().toLowerCase(Locale.ROOT))));
//        }));

        addButton(new ConsumerButtonWidget(width/2 - 75, height - 32, 150, 20, ScreenTexts.DONE, (screen, button) -> onClose()));
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        renderBackground();
        listWidget.render(mouseX, mouseY, delta);
        if (listWidget.getEntryCount() == 0)
            drawCenteredString(this.textRenderer, "▽◠▽.. :(", width / 2, height / 2, 16777215);
        super.render(mouseX, mouseY, delta);

        if (listWidget.tooltip != null) {
            renderTooltip(listWidget.tooltip, mouseX, mouseY);
            listWidget.tooltip = null;
        }
    }

    public void onClose() {
        if (this.client != null) this.client.openScreen(parent);
    }

    private class TimerSplitListWidget extends EntryListWidget {
        private List<String> tooltip = null;

        public TimerSplitListWidget() {
            super(TimerRecordListScreen.this.client,
                    TimerRecordListScreen.this.width, TimerRecordListScreen.this.height,
                    32, TimerRecordListScreen.this.height - 42, 24);
            this.applyFilter(TimerRecordListScreen.this.filter, TimerRecordListScreen.this.runOrder);
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


            widgetButtons.clear();
            ArrayList<TimerSplitEntry> entryArrayList = new ArrayList<>();
            for (TimerRecord timerRecord : splitList) {
                TimerSplitEntry entry = new TimerSplitEntry(timerRecord);
                widgetButtons.add(entry.deleteButton);
                entryArrayList.add(entry);
            }
            entries.clear();
            entries.addAll(entryArrayList);
            scroll(0);
        }

        private final ArrayList<TimerSplitEntry> entries = new ArrayList<>();
        @Override
        protected int getEntryCount() {
            return entries.size();
        }

        @Override
        public TimerSplitEntry getEntry(int index) {
            return entries.get(index);
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 100;
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 50;
        }

        private class TimerSplitEntry implements Entry {
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
                        .setStyle(new Style().setFormatting(Formatting.GRAY));
                for (Map.Entry<String, Long> splitPoint : split.getSplitTimeline().entrySet()) {
                    RunSplitType splitType = RunSplitType.getSplitType(splitPoint.getKey());
                    String time = InGameTimer.timeToStringFormat(splitPoint.getValue());
                    if (splitType == RunSplitTypes.COMPLETE) {
                        resultTimeText = new LiteralText("§a" + I18n.translate("speedrunigt.split.run_end") + ": §f" + time);
                    } else {
                        timelineTextList.add(new LiteralText("§b" + I18n.translate(splitType.getTranslateKey()) + ": §f" + time).asFormattedString());
                    }
                }
                this.deleteButton = new ConsumerButtonWidget(0, 0, 40, 20, new TranslatableText("selectWorld.delete").getString(), (screen, buttonWidget) -> {
                    if (Objects.equals(buttonWidget.message, new TranslatableText("selectWorld.delete").getString())) {
                        buttonWidget.message = new TranslatableText("selectWorld.delete").append("?").getString();
                    } else {
                        split.delete();
                        TimerSplitListWidget.this.entries.remove(this);
                    }
                });
            }

            private boolean isMouseOver(int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {
                return x < mouseX && x + entryWidth > mouseX && y < mouseY && y + entryHeight > mouseY;
            }

            @Override
            public void method_6700(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                this.deleteButton.x = x + entryWidth - this.deleteButton.getWidth();
                this.deleteButton.y = y + 2;
                this.deleteButton.method_891(TimerRecordListScreen.this.client, mouseX, mouseY, tickDelta);

                TimerRecordListScreen.this.textRenderer.draw(titleText.asFormattedString(), x, y + 4, 16777215);
                if (timelineTextList.size() > 0) {
                    int count = (int) ((System.currentTimeMillis() - entryCreatedTime) / 2500) % timelineTextList.size();
                    TimerRecordListScreen.this.textRenderer.draw(resultTimeText.copy().append("  ").append(timelineTextList.get(count)).asFormattedString(), x, y + 13, 16777215);
                } else {
                    TimerRecordListScreen.this.textRenderer.draw(resultTimeText.asFormattedString(), x, y + 13, 16777215);
                }
                drawCenteredString(TimerRecordListScreen.this.textRenderer, "§8§m                                                                                ",
                        x + (entryWidth / 2), y + 20, 16777215);

                ArrayList<String> tooltip = new ArrayList<>();
                if (this.deleteButton.isMouseOver(TimerRecordListScreen.this.client, mouseX, mouseY) && !Objects.equals(this.deleteButton.message, I18n.translate("selectWorld.delete"))) {
                    tooltip.add(new TranslatableText("speedrunigt.message.click_delete_button_again").asFormattedString());
                    TimerSplitListWidget.this.tooltip = tooltip;
                } else if (isMouseOver(x, y, entryWidth, entryHeight, mouseX, mouseY)) {
                    tooltip.add(titleText.copy().setStyle(new Style().setFormatting(Formatting.YELLOW)).asFormattedString());
                    tooltip.add(new LiteralText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time))).setStyle(new Style().setFormatting(Formatting.GRAY)).asFormattedString());
                    if (split.getRunType() != RunType.SET_SEED && split.getRunType() != RunType.SAVED_WORLD)
                        tooltip.add(new LiteralText(I18n.translate("commands.seed.success", split.getSeed())).setStyle(new Style().setFormatting(Formatting.GRAY)).asFormattedString());
                    tooltip.add("");
                    if (timelineTextList.size() > 0) {
                        tooltip.addAll(timelineTextList);
                        tooltip.add("");
                    }
                    tooltip.add(resultTimeText.asFormattedString());
                    TimerSplitListWidget.this.tooltip = tooltip;
                }
            }

            @Override
            public void method_9473(int i, int j, int k, float f) {

            }

            @Override
            public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
                return false;
            }

            @Override
            public void mouseReleased(int index, int mouseX, int mouseY, int button, int x, int y) {

            }
        }
    }
}
