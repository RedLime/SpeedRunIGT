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
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
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
        CustomCategoryManager.init(false);
        this.parent = parent;
    }

    @Override
    public void init() {
        buttons.add(new ConsumerButtonWidget(width / 2 - 100, height - 35, 200, 20, ScreenTexts.CANCEL, (button) -> onClose()));
        this.listWidget = new CategorySelectionListWidget(this.client);
    }

    public void onClose() {
        if (this.client != null) this.client.openScreen(parent);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
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

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.listWidget.render(mouseX, mouseY, delta);
        this.drawCenteredString(this.textRenderer, new TranslatableText("speedrunigt.option.timer_category").asFormattedString(), this.width / 2, 16, 16777215);
        this.drawCenteredString(this.textRenderer, "(" + I18n.translate("speedrunigt.option.timer_category.warning") + ")", this.width / 2, this.height - 46, 8421504);
        super.render(mouseX, mouseY, delta);
    }

    @Environment(EnvType.CLIENT)
    class CategorySelectionListWidget extends EntryListWidget {
        private final ArrayList<CategoryEntry> entries = new ArrayList<>();
        public CategorySelectionListWidget(MinecraftClient minecraft) {
            super(minecraft, SpeedRunCategoryScreen.this.width, SpeedRunCategoryScreen.this.height, 32, SpeedRunCategoryScreen.this.height - 55, 24);
            entries.addAll(RunCategory.getCategories().values().stream().filter(runCategory -> !runCategory.isHideCategory()).map(CategoryEntry::new).collect(Collectors.toList()));
            widgetButtons.addAll(entries.stream().map(entry -> entry.checkBox).collect(Collectors.toList()));
        }

        @Override
        public Entry getEntry(int index) {
            return entries.get(index);
        }

        @Override
        protected int getEntryCount() {
            return entries.size();
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 30;
        }

        @Environment(EnvType.CLIENT)
        public class CategoryEntry implements Entry {

            private final ConsumerButtonWidget checkBox;
            private final RunCategory category;

            public CategoryEntry(RunCategory category) {
                this.checkBox = new ConsumerButtonWidget(0, 0, 20, 20, "", (button) -> {
                    button.playDownSound(SpeedRunCategoryScreen.this.client.getSoundManager());
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_CATEGORY, category);
                    InGameTimer.getInstance().setCategory(category, true);
                    InGameTimer.getInstance().setUncompleted(true);
                });
                this.category = category;
            }

            @Override
            public void method_9473(int index, int x, int y, float f) {

            }

            @Override
            public void method_6700(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float f) {
                this.checkBox.x = x + 34;
                this.checkBox.y = y;
                this.checkBox.message = (InGameTimer.getInstance().getStatus() != TimerStatus.NONE ? InGameTimer.getInstance().getCategory() : SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY)) == this.category ? new LiteralText("█").setStyle(new Style().setBold(true)).asFormattedString() : "";
                this.checkBox.method_891(MinecraftClient.getInstance(), mouseX, mouseY, f);
                drawWithShadow(SpeedRunCategoryScreen.this.textRenderer, category.getText().asFormattedString(), this.checkBox.x + 24, this.checkBox.y + 6,14737632 | 255 << 24);
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
