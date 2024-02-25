package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.CustomCategoryManager;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableTextContent;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SpeedRunCategoryScreen extends Screen {

    private final Screen parent;
    private CategorySelectionListWidget listWidget;
    private final ArrayList<ClickableWidget> widgetButtons = new ArrayList<>();

    public SpeedRunCategoryScreen(Screen parent) {
        CustomCategoryManager.init(false);
        this.parent = parent;
    }

    @Override
    public void method_2224() {
        field_2564.add(new ConsumerButtonWidget(field_2561 / 2 - 100, field_2559 - 35, 200, 20, ScreenTexts.CANCEL, (button) -> onClose()));
        this.listWidget = new CategorySelectionListWidget(this.field_2563);
    }

    public void onClose() {
        if (this.field_2563 != null) this.field_2563.setScreen(parent);
    }

    @Override
    protected void method_0_2778(ClickableWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.method_0_2778(button);
    }

    @Override
    public void method_0_2801() {
        super.method_0_2801();
        this.listWidget.method_0_2650();
    }

    @Override
    protected void method_0_2775(int mouseX, int mouseY, int button) {
        ArrayList<ClickableWidget> widgets = new ArrayList<>(widgetButtons);
        field_2564.addAll(widgets);
        super.method_0_2775(mouseX, mouseY, button);
        field_2564.removeAll(widgets);
    }

    @Override
    public void method_2214(int mouseX, int mouseY, float delta) {
        this.listWidget.method_1930(mouseX, mouseY, delta);
        this.method_1789(this.field_2554, new TranslatableTextContent("speedrunigt.option.timer_category").method_10865(), this.field_2561 / 2, 16, 16777215);
        this.method_1789(this.field_2554, "(" + I18n.translate("speedrunigt.option.timer_category.warning") + ")", this.field_2561 / 2, this.field_2559 - 46, 8421504);
        super.method_2214(mouseX, mouseY, delta);
    }

    @Environment(EnvType.CLIENT)
    class CategorySelectionListWidget extends EntryListWidget {
        private final ArrayList<CategoryEntry> entries = new ArrayList<>();
        public CategorySelectionListWidget(MinecraftClient minecraft) {
            super(minecraft, SpeedRunCategoryScreen.this.field_2561, SpeedRunCategoryScreen.this.field_2559, 32, SpeedRunCategoryScreen.this.field_2559 - 55, 24);
            entries.addAll(RunCategory.getCategories().values().stream().filter(runCategory -> !runCategory.isHideCategory()).map(CategoryEntry::new).collect(Collectors.toList()));
            widgetButtons.addAll(entries.stream().map(entry -> entry.checkBox).collect(Collectors.toList()));
        }

        @Override
        public class_0_701 method_0_2558(int index) {
            return entries.get(index);
        }

        @Override
        protected int method_1947() {
            return entries.size();
        }

        @Override
        protected int method_1948() {
            return super.method_1948() + 30;
        }

        @Environment(EnvType.CLIENT)
        public class CategoryEntry implements class_0_701 {

            private final ConsumerButtonWidget checkBox;
            private final RunCategory category;

            public CategoryEntry(RunCategory category) {
                this.checkBox = new ConsumerButtonWidget(0, 0, 20, 20, "", (button) -> {
                    button.method_1832(SpeedRunCategoryScreen.this.field_2563.getSoundManager());
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_CATEGORY, category);
                    InGameTimer.getInstance().setCategory(category, true);
                    InGameTimer.getInstance().setUncompleted(true);
                });
                this.category = category;
            }

            @Override
            public void method_1904(int index, int x, int y, float f) {

            }

            @Override
            public void method_1903(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float f) {
                this.checkBox.field_2069 = x + 34;
                this.checkBox.field_2068 = y;
                this.checkBox.field_2074 = (InGameTimer.getInstance().getStatus() != TimerStatus.NONE ? InGameTimer.getInstance().getCategory() : SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY)) == this.category ? new LiteralTextContent("█").setStyle(new Style().withBold(true)).method_10865() : "";
                this.checkBox.method_1824(MinecraftClient.getInstance(), mouseX, mouseY, f);
                method_1780(SpeedRunCategoryScreen.this.field_2554, category.getText().method_10865(), this.checkBox.field_2069 + 24, this.checkBox.field_2068 + 6,14737632 | 255 << 24);
            }

            @Override
            public boolean method_0_2562(int index, int mouseX, int mouseY, int button, int x, int y) {
                return false;
            }

            @Override
            public void method_0_2564(int index, int mouseX, int mouseY, int button, int x, int y) {

            }
        }
    }
}
