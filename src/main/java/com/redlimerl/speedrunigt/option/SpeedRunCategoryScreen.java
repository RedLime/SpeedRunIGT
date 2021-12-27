package com.redlimerl.speedrunigt.option;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.version.CheckboxWidget;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.ListWidget;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import java.util.*;
import java.util.stream.Collectors;
/**
 * @author Void_X_Walker
 * @reason Backported to 1.8, redid almost everything because 1.8 screens and buttons work completely different
 */
public class SpeedRunCategoryScreen extends Screen {

    private final Screen parent;
    public static Map<Integer,RunCategory> map= new HashMap<>();
    private CategorySelectionListWidget listWidget;
    protected String title = "timer category options";
    public SpeedRunCategoryScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    public void init() {
        assert client != null;
        buttons.add(new ButtonWidget(7778,width / 2 - 100, height - 35, 200, 20, ScreenTexts.CANCEL));
        this.listWidget = new CategorySelectionListWidget(client);
    }

    protected void buttonClicked(ButtonWidget button) {
        if(button.id==7778){
            client.openScreen(parent);
        }
        else if(button.id==7789){
            ((CheckboxWidget)button).onPress();
        }

    }
    public void handleMouse() {
        super.handleMouse();
        this.listWidget.handleMouse();
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {



        this.listWidget.render(mouseX, mouseY, delta);
        this.drawCenteredString( this.textRenderer, this.title, this.width / 2, 16, 16777215);
        this.drawCenteredString(this.textRenderer, "(" + SpeedRunIGT.translate("speedrunigt.option.timer_category.warning","There are several unsupported categories, which will be added later.").getString() + ")", this.width / 2, this.height - 46, 8421504);


        super.render(mouseX, mouseY, delta);

    }
    @Environment(EnvType.CLIENT)
    class CategorySelectionListWidget extends ListWidget {
        List<CategoryEntry> entries = Lists.newArrayList();
        public CategorySelectionListWidget(MinecraftClient minecraft) {
            super(minecraft, SpeedRunCategoryScreen.this.width, SpeedRunCategoryScreen.this.height, 32, SpeedRunCategoryScreen.this.height - 55, 24);
            entries.clear();
           entries=Arrays.stream(RunCategory.values()).map(CategoryEntry::new).collect(Collectors.toList());
        }

        @Override
        protected int getEntryCount() {
            return entries.size();
        }

        @Override
        protected void selectEntry(int index, boolean bl, int lastMouseX, int lastMouseY) {
            entries.get(index).checkBox.onPress();
        }

        @Override
        protected boolean isEntrySelected(int index) {
            return false;
        }

        @Override
        protected void renderBackground() {

        }

        @Override
        protected void renderEntry(int index, int x, int y, int rowHeight, int mouseX, int mouseY) {
            this.entries.get(index).checkBox.x = x + 34;
            this.entries.get(index).checkBox.y = y;
            this.entries.get(index).checkBox.render(MinecraftClient.getInstance(),mouseX, mouseY);
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 30;
        }



        @Environment(EnvType.CLIENT)
        public class CategoryEntry implements EntryListWidget.Entry {

            private final ArrayList<ButtonWidget> children = new ArrayList<>();
            private final CategoryCheckBoxWidget checkBox;
            public CategoryEntry(RunCategory category) {
                this.checkBox = new CategoryCheckBoxWidget(category);
                children.add(checkBox);
            }

            @Override
            public void updatePosition(int index, int x, int y) {

            }

            public void render(int index, int x,int y,  int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered) {
                this.checkBox.x = x + 34;
                this.checkBox.y = y;
                this.checkBox.render(MinecraftClient.getInstance(),mouseX, mouseY);
            }

            @Override
            public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int x, int y) {
                return false;
            }

            @Override
            public void mouseReleased(int index, int mouseX, int mouseY, int button, int x, int y) {

            }


            private class CategoryCheckBoxWidget extends CheckboxWidget {

                private final RunCategory category;

                public CategoryCheckBoxWidget(RunCategory category) {
                    super(7789,0, 0, 20, 20, category.getText().asFormattedString(), false);
                    this.category = category;
                }

                @Override
                public void onPress() {
                    super.onPress();
                    SpeedRunOptions.setOption(SpeedRunOptions.TIMER_CATEGORY, this.category);
                    InGameTimer.getInstance().setCategory(this.category);
                    InGameTimer.getInstance().setUncompleted();
                }

                @Override
                public boolean isChecked() {
                    return SpeedRunOptions.getOption(SpeedRunOptions.TIMER_CATEGORY) == category;
                }

                @Override
                public void render(MinecraftClient client,int mouseX, int mouseY) {
                    MinecraftClient minecraftClient = MinecraftClient.getInstance();
                    minecraftClient.getTextureManager().bindTexture( new Identifier("textures/gui/widgets.png"));
                    GlStateManager.enableDepthTest();
                    TextRenderer textRenderer = minecraftClient.textRenderer;
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.enableBlend();
                    GlStateManager.blendFuncSeparate(770, 771, 1, 0);
                    GlStateManager.blendFunc(770, 771);
                    int i = this.isChecked()?2:1;
                    this.drawTexture(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
                    this.drawTexture(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
                    this.renderBg(minecraftClient, mouseX, mouseY);
                    this.drawWithShadow(textRenderer, this.message, this.x + 24, this.y + (this.height - 8) / 2, 14737632 | MathHelper.ceil(1.0F * 255.0F) << 24);
                }
            }
        }
    }
}
