package com.redlimerl.speedrunigt.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.CustomCategoryManager;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpeedRunCategoryScreen extends Screen {

    private final Screen parent;
    private CategorySelectionListWidget listWidget;

    public SpeedRunCategoryScreen(Screen parent) {
        super(Text.translatable("speedrunigt.option.timer_category"));
        CustomCategoryManager.init(false);
        this.parent = parent;
    }

    @Override
    protected void init() {
        assert client != null;
        addDrawableChild(ButtonWidgetHelper.create(width / 2 - 100, height - 35, 200, 20, ScreenTexts.CANCEL, button -> client.setScreen(parent)));

        this.listWidget = new CategorySelectionListWidget(client);
        addSelectableChild(listWidget);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.listWidget.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 16777215);
        context.drawCenteredTextWithShadow(this.textRenderer, "(" + I18n.translate("speedrunigt.option.timer_category.warning") + ")", this.width / 2, this.height - 46, 8421504);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
    }

    @Environment(EnvType.CLIENT)
    class CategorySelectionListWidget extends ElementListWidget<CategorySelectionListWidget.CategoryEntry> {
        private final List<CategoryEntry> entryList = Lists.newArrayList();
        public CategorySelectionListWidget(MinecraftClient client) {
            super(client, SpeedRunCategoryScreen.this.width, SpeedRunCategoryScreen.this.height - 87, 32, 24);

            entryList.addAll(RunCategory.getCategories().values().stream().filter(runCategory -> !runCategory.isHideCategory()).map(CategoryEntry::new).toList());
            this.replaceEntries(entryList);
        }

        @Override
        protected int getScrollbarPositionX() {
            return super.getScrollbarPositionX() + 30;
        }

        @Environment(EnvType.CLIENT)
        public class CategoryEntry extends ElementListWidget.Entry<CategoryEntry> {

            private final ArrayList<ClickableWidget> children = new ArrayList<>();
            private final CheckboxWidget checkBox;
            private final ButtonWidget urlButton;

            public CategoryEntry(RunCategory category) {
                this.checkBox = CheckboxWidget.builder(category.getText(), textRenderer)
                        .checked((InGameTimer.getInstance().getStatus() != TimerStatus.NONE ? InGameTimer.getInstance().getCategory()
                                : SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY)) == category)
                        .callback((checkbox, checked) -> {
                            entryList.forEach(entry -> {
                                if (entry.checkBox.isChecked()) entry.checkBox.onPress();
                            });
                            if (checked) {
                                checkbox.onPress();
                                SpeedRunOption.setOption(SpeedRunOptions.TIMER_CATEGORY, category);
                                InGameTimer.getInstance().setCategory(category, true);
                                InGameTimer.getInstance().setUncompleted(true);
                            }
                        })
                        .pos(0, 0)
                        .build();
                this.urlButton = ButtonWidgetHelper.create(0, 0, 30, 20, Text.translatable("speedrunigt.option.more"), button -> Util.getOperatingSystem().open(category.getLeaderboardUrl()));
                children.add(urlButton);
                children.add(checkBox);
            }

            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                this.urlButton.setX(x);
                this.urlButton.setY(y);
                this.urlButton.render(context, mouseX, mouseY, tickDelta);
                this.checkBox.setX(x + 34);
                this.checkBox.setY(y);
                this.checkBox.render(context, mouseX, mouseY, tickDelta);
            }

            @Override
            public List<? extends Element> children() {
                return children;
            }

            @Override
            public List<? extends Selectable> selectableChildren() {
                return children;
            }

        }
    }
}
