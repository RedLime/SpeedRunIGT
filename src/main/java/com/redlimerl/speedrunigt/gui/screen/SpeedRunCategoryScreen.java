package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1803;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class SpeedRunCategoryScreen extends Screen {

    private final Screen parent;
    private CategorySelectionListWidget listWidget;
    private final ArrayList<ButtonWidget> widgetButtons = new ArrayList<>();

    public SpeedRunCategoryScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    public void method_21947() {
        field_22537.add(new ConsumerButtonWidget(field_22535 / 2 - 100, field_22536 - 35, 200, 20, ScreenTexts.CANCEL, (button) -> onClose()));
        this.listWidget = new CategorySelectionListWidget(this.field_22534);
    }

    public void onClose() {
        if (this.field_22534 != null) this.field_22534.openScreen(parent);
    }

    @Override
    protected void method_21930(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.method_21930(button);
    }

    @Override
    protected void method_21926(int mouseX, int mouseY, int button) {
        ArrayList<ButtonWidget> widgets = new ArrayList<>(widgetButtons);
        field_22537.addAll(widgets);
        super.method_21926(mouseX, mouseY, button);
        field_22537.removeAll(widgets);
    }

    @Override
    public void method_21925(int mouseX, int mouseY, float delta) {
        this.listWidget.method_21897(mouseX, mouseY, delta);
        this.method_21881(this.field_22540, new TranslatableText("speedrunigt.option.timer_category").asFormattedString(), this.field_22535 / 2, 16, 16777215);
        this.method_21881(this.field_22540, "(" + I18n.translate("speedrunigt.option.timer_category.warning") + ")", this.field_22535 / 2, this.field_22536 - 46, 8421504);
        super.method_21925(mouseX, mouseY, delta);
    }

    @Environment(EnvType.CLIENT)
    class CategorySelectionListWidget extends EntryListWidget {
        private final ArrayList<CategoryEntry> entries = new ArrayList<>();
        public CategorySelectionListWidget(MinecraftClient minecraft) {
            super(minecraft, SpeedRunCategoryScreen.this.field_22535, SpeedRunCategoryScreen.this.field_22536, 32, SpeedRunCategoryScreen.this.field_22536 - 55, 24);
            entries.addAll(RunCategory.getCategories().values().stream().map(CategoryEntry::new).collect(Collectors.toList()));
            widgetButtons.addAll(entries.stream().map(entry -> entry.checkBox).collect(Collectors.toList()));
        }

        @Override
        public class_1803 method_6697(int i) {
            return entries.get(i);
        }

        @Override
        protected int method_21905() {
            return entries.size();
        }

        @Override
        protected int method_21912() {
            return super.method_21912() + 30;
        }

        @Environment(EnvType.CLIENT)
        public class CategoryEntry implements class_1803 {

            private final ConsumerButtonWidget checkBox;
            private final RunCategory category;

            public CategoryEntry(RunCategory category) {
                this.checkBox = new ConsumerButtonWidget(0, 0, 20, 20, "", (button) -> {
                    button.method_21888(SpeedRunCategoryScreen.this.field_22534.getSoundManager());
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_CATEGORY, category);
                    InGameTimer.getInstance().setCategory(category, true);
                    InGameTimer.getInstance().setUncompleted(true);
                });
                this.category = category;
            }

            @Override
            public void method_6700(int index, int x, int y, int rowWidth, int rowHeight, Tessellator tessellator, int mouseX, int mouseY, boolean hovered) {
                this.checkBox.x = x + 34;
                this.checkBox.y = y;
                this.checkBox.field_22510 = (InGameTimer.getInstance().getStatus() != TimerStatus.NONE ? InGameTimer.getInstance().getCategory() : SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY)) == this.category ? new LiteralText("█").setStyle(new Style().setBold(true)).asFormattedString() : "";
                this.checkBox.method_21887(MinecraftClient.getInstance(), mouseX, mouseY);
                SpeedRunCategoryScreen.this.field_22540.method_956(category.getText().asFormattedString(), this.checkBox.x + 24, this.checkBox.y + 6,14737632 | 255 << 24);
            }

            @Override
            public boolean method_6699(int i, int j, int k, int l, int m, int n) {
                return false;
            }

            @Override
            public void method_6701(int i, int j, int k, int l, int m, int n) {

            }
        }
    }
}
