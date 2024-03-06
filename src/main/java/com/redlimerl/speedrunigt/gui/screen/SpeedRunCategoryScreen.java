package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.gui.EntryWidget;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.CustomCategoryManager;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.Language;

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
        this.listWidget = new CategorySelectionListWidget(this.field_1229);
    }

    public void onClose() {
        if (this.field_1229 != null) this.field_1229.openScreen(parent);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.buttonClicked(button);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        ArrayList<ButtonWidget> widgets = new ArrayList<>(widgetButtons);
        buttons.addAll(widgets);
        super.mouseClicked(mouseX, mouseY, button);
        buttons.removeAll(widgets);
        listWidget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        listWidget.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.listWidget.render(mouseX, mouseY, delta);
        this.drawCenteredString(this.textRenderer, Language.getInstance().translate("speedrunigt.option.timer_category"), this.width / 2, 16, 16777215);
        this.drawCenteredString(this.textRenderer, "(" + Language.getInstance().translate("speedrunigt.option.timer_category.warning") + ")", this.width / 2, this.height - 46, 8421504);
        super.render(mouseX, mouseY, delta);
    }

    @Environment(EnvType.CLIENT)
    class CategorySelectionListWidget extends EntryListWidget {
        private final ArrayList<CategoryEntry> entries = new ArrayList<>();
        public CategorySelectionListWidget(Minecraft minecraft) {
            super(minecraft, SpeedRunCategoryScreen.this.width, SpeedRunCategoryScreen.this.height, 32, SpeedRunCategoryScreen.this.height - 55, 24);
            entries.addAll(RunCategory.getCategories().values().stream().filter(runCategory -> !runCategory.isHideCategory()).map(CategoryEntry::new).collect(Collectors.toList()));
            widgetButtons.addAll(entries.stream().map(entry -> entry.checkBox).collect(Collectors.toList()));
        }

        @Override
        protected int getEntryCount() {
            return entries.size();
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 30;
        }

        @Override
        public EntryWidget getEntry(int i) {
            return entries.get(i);
        }

        @Environment(EnvType.CLIENT)
        public class CategoryEntry implements EntryWidget {

            private final ConsumerButtonWidget checkBox;
            private final RunCategory category;

            public CategoryEntry(RunCategory category) {
                this.checkBox = new ConsumerButtonWidget(0, 0, 20, 20, "", (button) -> {
                    Minecraft.getMinecraft().soundSystem.playSound("random.click", 1.0F, 1.0F);
                    SpeedRunOption.setOption(SpeedRunOptions.TIMER_CATEGORY, category);
                    InGameTimer.getInstance().setCategory(category, true);
                    InGameTimer.getInstance().setUncompleted(true);
                });
                this.category = category;
            }

            @Override
            public void draw(int index, int x, int y, int rowWidth, int rowHeight, Tessellator tessellator, int mouseX, int mouseY, boolean hovered) {
                this.checkBox.x = x + 34;
                this.checkBox.y = y;
                this.checkBox.message = (InGameTimer.getInstance().getStatus() != TimerStatus.NONE ? InGameTimer.getInstance().getCategory() : SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY)) == this.category ? "█" : "";
                this.checkBox.method_891(Minecraft.getMinecraft(), mouseX, mouseY);
                drawWithShadow(SpeedRunCategoryScreen.this.textRenderer, category.getText(), this.checkBox.x + 24, this.checkBox.y + 6,14737632 | 255 << 24);
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
