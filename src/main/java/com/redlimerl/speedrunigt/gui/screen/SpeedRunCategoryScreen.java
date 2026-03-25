package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.CustomCategoryManager;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Util;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.List;

public class SpeedRunCategoryScreen extends Screen {

    private final Screen parent;
    private CategorySelectionListWidget listWidget;

    public SpeedRunCategoryScreen(Screen parent) {
        super(Component.translatable("speedrunigt.option.timer_category"));
        CustomCategoryManager.init(false);
        this.parent = parent;
    }

    @Override
    protected void init() {
        assert minecraft != null;
        addRenderableWidget(ButtonWidgetHelper.create(width / 2 - 100, height - 35, 200, 20, CommonComponents.GUI_CANCEL, button -> minecraft.setScreen(parent)));

        this.listWidget = new CategorySelectionListWidget(minecraft);
        addWidget(listWidget);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.listWidget.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 16, CommonColors.WHITE);
        context.drawCenteredString(this.font, "(" + I18n.get("speedrunigt.option.timer_category.warning") + ")", this.width / 2, this.height - 46, 8421504);
    }

    @Environment(EnvType.CLIENT)
    class CategorySelectionListWidget extends ContainerObjectSelectionList<CategorySelectionListWidget.CategoryEntry> {
        private final List<CategoryEntry> entryList = Lists.newArrayList();
        public CategorySelectionListWidget(Minecraft client) {
            super(client, SpeedRunCategoryScreen.this.width, SpeedRunCategoryScreen.this.height - 87, 32, 24);

            entryList.addAll(RunCategory.getCategories().values().stream().filter(runCategory -> !runCategory.isHideCategory()).map(CategoryEntry::new).toList());
            this.replaceEntries(entryList);
        }

        @Override
        protected int scrollBarX() {
            return super.scrollBarX() + 30;
        }

        public static class EmptyInput implements InputWithModifiers {
            @Override
            public int input() {
                return 0;
            }

            @Override
            public int modifiers() {
                return 0;
            }
        }

        @Environment(EnvType.CLIENT)
        public class CategoryEntry extends ContainerObjectSelectionList.Entry<CategoryEntry> {

            private final ArrayList<AbstractWidget> children = new ArrayList<>();
            private final Checkbox checkBox;
            private final Button urlButton;

            public CategoryEntry(RunCategory category) {
                this.checkBox = Checkbox.builder(category.getText(), font)
                        .selected((InGameTimer.getInstance().getStatus() != TimerStatus.NONE ? InGameTimer.getInstance().getCategory()
                                : SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY)) == category)
                        .onValueChange((checkbox, checked) -> {
                            // CheckboxWidget#onPress both toggles the checkbox and runs this callback,
                            // so we just ignore any calls from checkboxes that are being disabled
                           if (!checked) {
                               // disallow disabling the selected checkbox by re-selecting it if it is deselected
                               if (entryList.stream().noneMatch(categoryEntry -> categoryEntry.checkBox.selected())) {
                                   checkbox.onPress(new EmptyInput());
                               }
                               return;
                           }
                            for (CategoryEntry entry : entryList) {
                                // make sure we're not unchecking the one we just checked
                                if (entry.checkBox.selected() && entry != this) {
                                    entry.checkBox.onPress(new EmptyInput());
                                }
                            }
                            SpeedRunOption.setOption(SpeedRunOptions.TIMER_CATEGORY, category);
                            InGameTimer.getInstance().setCategory(category, true);
                            InGameTimer.getInstance().setUncompleted(true);
                        })
                        .pos(0, 0)
                        .build();
                this.urlButton = ButtonWidgetHelper.create(0, 0, 30, 20, Component.translatable("speedrunigt.option.more"), button -> Util.getPlatform().openUri(category.getLeaderboardUrl()));
                children.add(urlButton);
                children.add(checkBox);
            }

            @Override
            public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
                this.urlButton.setX(this.getX());
                this.urlButton.setY(this.getY());
                this.urlButton.render(context, mouseX, mouseY, deltaTicks);
                this.checkBox.setX(this.getX() + 34);
                this.checkBox.setY(this.getY());
                this.checkBox.render(context, mouseX, mouseY, deltaTicks);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return children;
            }

        }
    }
}
