package com.redlimerl.speedrunigt.option;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.version.CheckboxWidget;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1803;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ListWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Void_X_Walker
 * @reason Backported to 1.8, redid almost everything because 1.8 screens and buttons work completely different
 */
@SuppressWarnings("unchecked")
public class SpeedRunCategoryScreen extends Screen {

    private final Screen parent;
    private CategorySelectionListWidget listWidget;
    protected String title = "Timer Category Options";
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
        List<CategoryEntry> entries;
        public CategorySelectionListWidget(MinecraftClient minecraft) {
            super(minecraft, SpeedRunCategoryScreen.this.width, SpeedRunCategoryScreen.this.height, 32, SpeedRunCategoryScreen.this.height - 55, 24);
            entries = Arrays.stream(RunCategory.values()).map(CategoryEntry::new).collect(Collectors.toList());
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
        protected void method_1055(int index, int x, int y, int rowHeight, Tessellator tessellator, int mouseX, int mouseY) {
            this.entries.get(index).checkBox.x = x + 34;
            this.entries.get(index).checkBox.y = y;
            this.entries.get(index).checkBox.render(MinecraftClient.getInstance(),mouseX, mouseY);
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 30;
        }



        @Environment(EnvType.CLIENT)
        public class CategoryEntry implements class_1803 {

            private final CategoryCheckBoxWidget checkBox;
            public CategoryEntry(RunCategory category) {
                this.checkBox = new CategoryCheckBoxWidget(category);
            }

            @Override
            public void method_6700(int i, int j, int k, int l, int m, Tessellator tessellator, int n, int o, boolean bl) {
                this.checkBox.x = j + 34;
                this.checkBox.y = k;
                this.checkBox.render(MinecraftClient.getInstance(),n, o);
            }

            @Override
            public boolean method_6699(int i, int j, int k, int l, int m, int n) {
                return false;
            }

            @Override
            public void method_6701(int i, int j, int k, int l, int m, int n) {

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
                    TextRenderer textRenderer = minecraftClient.textRenderer;
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glBlendFunc(770, 771);
                    int i = this.isChecked()?2:1;
                    this.drawTexture(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
                    this.drawTexture(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
                    this.renderBg(minecraftClient, mouseX, mouseY);
                    this.drawWithShadow(textRenderer, this.message, this.x + 24, this.y + (this.height - 8) / 2, 14737632 | 255 << 24);
                }
            }
        }
    }
}
