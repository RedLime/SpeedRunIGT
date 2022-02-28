package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.running.RunCategory;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;

public class SpeedRunCategoryScreen extends Screen {

    private final Screen parent;
    private CategorySelectionListWidget listWidget;
    private final ArrayList<ButtonWidget> widgetButtons = new ArrayList<>();

    public SpeedRunCategoryScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    public void init() {
        method_13411(new ConsumerButtonWidget(width / 2 - 100, height - 35, 200, 20, ScreenTexts.CANCEL, (button) -> method_18608()));
        this.listWidget = new CategorySelectionListWidget(this.client);
    }

    @Override
    public void method_18608() {
        if (this.client != null) this.client.openScreen(parent);
    }

    public boolean mouseScrolled(double d) {
        return this.listWidget.mouseScrolled(d);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        ArrayList<ButtonWidget> widgets = new ArrayList<>(widgetButtons);
        buttons.addAll(widgets);
        boolean c = super.mouseClicked(d, e, i);
        buttons.removeAll(widgets);
        return c;
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.listWidget.render(mouseX, mouseY, delta);
        this.drawCenteredString(this.textRenderer, new TranslatableText("speedrunigt.option.timer_category").asFormattedString(), this.width / 2, 16, 16777215);
        this.drawCenteredString(this.textRenderer, "(" + I18n.translate("speedrunigt.option.timer_category.warning") + ")", this.width / 2, this.height - 46, 8421504);
        super.render(mouseX, mouseY, delta);
    }

    @Environment(EnvType.CLIENT)
    class CategorySelectionListWidget extends EntryListWidget<CategorySelectionListWidget.CategoryEntry> {
        public CategorySelectionListWidget(MinecraftClient minecraft) {
            super(minecraft, SpeedRunCategoryScreen.this.width, SpeedRunCategoryScreen.this.height, 32, SpeedRunCategoryScreen.this.height - 55, 24);
            for (RunCategory runCategory : RunCategory.getCategories().values()) {
                CategoryEntry entry = new CategoryEntry(runCategory);
                field_20307.add(entry);
                this.method_18423().add(entry);
                widgetButtons.add(entry.checkBox);
                SpeedRunCategoryScreen.this.field_20307.add(entry.checkBox);
            }
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 30;
        }

        @Environment(EnvType.CLIENT)
        public class CategoryEntry extends EntryListWidget.Entry<CategoryEntry> {

            private final ConsumerButtonWidget checkBox;
            private final RunCategory category;

            public CategoryEntry(RunCategory category) {
                this.checkBox = new ConsumerButtonWidget(0, 0, 20, 20, "", (button) -> {
                    button.playDownSound(SpeedRunCategoryScreen.this.client.getSoundManager());
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_CATEGORY, category);
                    InGameTimer.getInstance().setCategory(category);
                    InGameTimer.getInstance().setUncompleted();
                });
                this.category = category;
            }

            @Override
            public void method_6700(int i, int j, int k, int l, boolean bl, float f) {
                int n = this.method_18404();
                int m = this.method_18403();
                this.checkBox.x = n + 34;
                this.checkBox.y = m;
                this.checkBox.message = SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY) == this.category ? new LiteralText("█").setStyle(new Style().setBold(true)).asFormattedString() : "";
                this.checkBox.method_891(k, l, f);
                drawWithShadow(SpeedRunCategoryScreen.this.textRenderer, category.getText().asFormattedString(), this.checkBox.x + 24, this.checkBox.y + 6,14737632 | 255 << 24);
            }
        }
    }
}
