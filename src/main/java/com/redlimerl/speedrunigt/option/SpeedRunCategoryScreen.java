package com.redlimerl.speedrunigt.option;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpeedRunCategoryScreen extends Screen {

    private final Screen parent;
    private CategorySelectionListWidget listWidget;

    public SpeedRunCategoryScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.option.timer_category"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        assert minecraft != null;
        addButton(new ButtonWidget(width / 2 - 100, height - 35, 200, 20, ScreenTexts.CANCEL, button -> minecraft.openScreen(parent)));

        this.listWidget = new CategorySelectionListWidget(minecraft);
        children.add(listWidget);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.openScreen(parent);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.listWidget.render(mouseX, mouseY, delta);
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 16, 16777215);
        this.drawCenteredString(this.font, "(" + I18n.translate("speedrunigt.option.timer_category.warning") + ")", this.width / 2, this.height - 46, 8421504);
        super.render(mouseX, mouseY, delta);
    }

    @Environment(EnvType.CLIENT)
    class CategorySelectionListWidget extends ElementListWidget<CategorySelectionListWidget.CategoryEntry> {
        public CategorySelectionListWidget(MinecraftClient minecraft) {
            super(minecraft, SpeedRunCategoryScreen.this.width, SpeedRunCategoryScreen.this.height, 32, SpeedRunCategoryScreen.this.height - 55, 24);

            this.replaceEntries(Arrays.stream(RunCategory.values()).map(CategoryEntry::new).collect(Collectors.toList()));
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 30;
        }

        @Environment(EnvType.CLIENT)
        public class CategoryEntry extends ElementListWidget.Entry<CategoryEntry> {

            private final ArrayList<AbstractPressableButtonWidget> children = new ArrayList<>();
            private final CategoryCheckBoxWidget checkBox;
            private final ButtonWidget urlButton;

            public CategoryEntry(RunCategory category) {
                this.checkBox = new CategoryCheckBoxWidget(category);
                this.urlButton = new ButtonWidget(0, 0, 30, 20, new TranslatableText("speedrunigt.option.more").asFormattedString(), button -> Util.getOperatingSystem().open("https://www.speedrun.com/"+category.getCode()));
                children.add(urlButton);
                children.add(checkBox);
            }

            public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                this.urlButton.x = x;
                this.urlButton.y = y;
                this.urlButton.render(mouseX, mouseY, tickDelta);
                this.checkBox.x = x + 34;
                this.checkBox.y = y;
                this.checkBox.render(mouseX, mouseY, tickDelta);
            }

            @Override
            public List<? extends Element> children() {
                return children;
            }

            private class CategoryCheckBoxWidget extends CheckboxWidget {
                private final Identifier TEXTURE = new Identifier("textures/gui/checkbox.png");
                private final RunCategory category;

                public CategoryCheckBoxWidget(RunCategory category) {
                    super(0, 0, 20, 20, category.getText(), false);
                    this.category = category;
                }

                @Override
                public void onPress() {
                    super.onPress();
                    SpeedRunOptions.setOption(SpeedRunOptions.TIMER_CATEGORY, this.category);
                    InGameTimer.getInstance().setCategory(this.category);
                }

                @Override
                public boolean isChecked() {
                    return SpeedRunOptions.getOption(SpeedRunOptions.TIMER_CATEGORY) == category;
                }

                @Override
                public void render(int mouseX, int mouseY, float delta) {
                    MinecraftClient minecraftClient = MinecraftClient.getInstance();
                    minecraftClient.getTextureManager().bindTexture(TEXTURE);
                    RenderSystem.enableDepthTest();
                    TextRenderer textRenderer = minecraftClient.textRenderer;
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
                    blit(this.x, this.y, this.isFocused() ? 20.0F : 0.0F, this.isChecked() ? 20.0F : 0.0F, 20, this.height, 32, 64);
                    this.renderBg(minecraftClient, mouseX, mouseY);
                    drawString(textRenderer, this.getMessage(), this.x + 24, this.y + (this.height - 8) / 2, 14737632 | MathHelper.ceil(this.alpha * 255.0F) << 24);
                }
            }
        }
    }
}
