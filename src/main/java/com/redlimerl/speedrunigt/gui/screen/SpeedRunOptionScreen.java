package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.gui.EntryWidget;
import com.redlimerl.speedrunigt.mixins.access.ButtonWidgetAccessor;
import com.redlimerl.speedrunigt.mixins.access.ScreenAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.utils.OperatingUtils;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.function.Supplier;

public class SpeedRunOptionScreen extends Screen {

    private final Screen parent;
    private final HashMap<String, ArrayList<ButtonWidget>> categorySubButtons = new HashMap<>();
    private final LinkedHashMap<String, ButtonWidget> categorySelectButtons = new LinkedHashMap<>();
    private final HashMap<ButtonWidget, Supplier<String>> tooltips = new HashMap<>();
    private ButtonScrollListWidget buttonListWidget;
    private final ArrayList<ButtonWidget> widgetButtons = new ArrayList<>();
    private String currentSelectCategory = "";
    private int page = 0;
    private ButtonWidget prevPageButton = null;
    private ButtonWidget nextPageButton = null;

    public SpeedRunOptionScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    public void init() {
        super.init();
        categorySubButtons.clear();
        categorySelectButtons.clear();
        tooltips.clear();

        List<OptionButtonFactory> optionButtonFactoryList = SpeedRunOption.getOptionButtonFactories();

        int categoryCount = 0;

        for (OptionButtonFactory factory : optionButtonFactoryList) {
            OptionButtonFactory.Storage builder = factory.create(this).build();
            ButtonWidget button = builder.getButtonWidget();
            if (builder.getTooltip() != null) tooltips.put(button, builder.getTooltip());

            String category = builder.getCategory();
            ArrayList<ButtonWidget> categoryList = categorySubButtons.getOrDefault(category, new ArrayList<>());
            categoryList.add(button);
            categorySubButtons.put(category, categoryList);

            if (!categorySelectButtons.containsKey(category)) {
                ButtonWidget buttonWidget = new ConsumerButtonWidget(width - 110, 30 + ((categoryCount++ % 6) * 22), 80, 20, Language.getInstance().translate(category), (buttonWidget1) -> selectCategory(category));
                categorySelectButtons.put(category, buttonWidget);
                buttons.add(buttonWidget);
            }
        }

        prevPageButton = new ConsumerButtonWidget(width - 110, 30 + (6 * 22), 38, 20, "<", (button) -> openPage(-1));
        buttons.add(prevPageButton);

        nextPageButton = new ConsumerButtonWidget(width - 68, 30 + (6 * 22), 38, 20, ">", (button) -> openPage(+1));
        buttons.add(nextPageButton);

        openPage(page);

        buttons.add(new ConsumerButtonWidget(width - 85, height - 35, 70, 20, ScreenTexts.CANCEL, (button) -> onClose()));

        buttons.add(new ConsumerButtonWidget(15, height - 35, 70, 20, Language.getInstance().translate("speedrunigt.menu.donate"), (button) -> OperatingUtils.setUrl("https://ko-fi.com/redlimerl")));

        buttons.add(new ConsumerButtonWidget(88, height - 35, 140, 20, Language.getInstance().translate("speedrunigt.menu.crowdin"), (button) -> OperatingUtils.setUrl("https://crowdin.com/project/speedrunigt")));

        buttonListWidget = new ButtonScrollListWidget();

        if (!currentSelectCategory.isEmpty()) selectCategory(currentSelectCategory);
        else categorySelectButtons.keySet().stream().findFirst().ifPresent(this::selectCategory);
    }

    public void openPage(int num) {
        int maxPage = Math.max((categorySelectButtons.keySet().size() - 1) / 6, 0);
        this.page = MathHelper.clamp(this.page + num, 0, maxPage);

        int count = 0;
        for (ButtonWidget value : categorySelectButtons.values()) {
            value.visible = this.page * 6 <= count && (this.page + 1) * 6 > count;
            count++;
        }

        if (maxPage == 0) {
            prevPageButton.visible = false;
            nextPageButton.visible = false;
        } else {
            prevPageButton.visible = true;
            nextPageButton.visible = true;
            prevPageButton.active = !(this.page == 0);
            nextPageButton.active = !(maxPage == this.page);
        }
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
        this.buttonListWidget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        this.buttonListWidget.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        this.buttonListWidget.render(mouseX, mouseY, delta);
        super.render(mouseX, mouseY, delta);
        drawCenteredString(this.textRenderer, Language.getInstance().translate("speedrunigt.title.options"), this.width / 2, 10, 16777215);
        drawWithShadow(this.textRenderer, "v"+ SpeedRunIGT.MOD_VERSION, 4, 4, 16777215);

        ArrayList<String> tooltip = getToolTip(mouseX, mouseY);
        if (!tooltip.isEmpty() && ((ScreenAccessor) this).getPrevClickedButton() == null) this.drawTooltip(tooltip, 0, height);
    }

    protected void drawTooltip(String string, int i, int j) {
        this.drawTooltip(Collections.singletonList(string), i, j);
    }

    protected void drawTooltip(List<String> list, int i, int j) {
        if (!list.isEmpty()) {
            GL11.glDisable(32826);
            DiffuseLighting.disable();
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            int var4 = 0;

            for(String var6 : list) {
                int var7 = this.textRenderer.getStringWidth(var6);
                if (var7 > var4) {
                    var4 = var7;
                }
            }

            int var14 = i + 12;
            int var15 = j - 12;
            int var8 = 8;
            if (list.size() > 1) {
                var8 += 2 + (list.size() - 1) * 10;
            }

            if (var14 + var4 > this.width) {
                var14 -= 28 + var4;
            }

            if (var15 + var8 + 6 > this.height) {
                var15 = this.height - var8 - 6;
            }

            this.zOffset = 300.0F;
            int var9 = -267386864;
            this.fillGradient(var14 - 3, var15 - 4, var14 + var4 + 3, var15 - 3, var9, var9);
            this.fillGradient(var14 - 3, var15 + var8 + 3, var14 + var4 + 3, var15 + var8 + 4, var9, var9);
            this.fillGradient(var14 - 3, var15 - 3, var14 + var4 + 3, var15 + var8 + 3, var9, var9);
            this.fillGradient(var14 - 4, var15 - 3, var14 - 3, var15 + var8 + 3, var9, var9);
            this.fillGradient(var14 + var4 + 3, var15 - 3, var14 + var4 + 4, var15 + var8 + 3, var9, var9);
            int var10 = 1347420415;
            int var11 = (var10 & 16711422) >> 1 | var10 & 0xFF000000;
            this.fillGradient(var14 - 3, var15 - 3 + 1, var14 - 3 + 1, var15 + var8 + 3 - 1, var10, var11);
            this.fillGradient(var14 + var4 + 2, var15 - 3 + 1, var14 + var4 + 3, var15 + var8 + 3 - 1, var10, var11);
            this.fillGradient(var14 - 3, var15 - 3, var14 + var4 + 3, var15 - 3 + 1, var10, var10);
            this.fillGradient(var14 - 3, var15 + var8 + 2, var14 + var4 + 3, var15 + var8 + 3, var11, var11);

            for(int var12 = 0; var12 < list.size(); ++var12) {
                String var13 = (String)list.get(var12);
                this.textRenderer.method_956(var13, var14, var15, -1);
                if (var12 == 0) {
                    var15 += 2;
                }

                var15 += 10;
            }

            this.zOffset = 0.0F;
            GL11.glEnable(2896);
            GL11.glEnable(2929);
            DiffuseLighting.enableNormally();
            GL11.glEnable(32826);
        }
    }

    public ArrayList<String> getToolTip(int mouseX, int mouseY) {
        ArrayList<String> tooltipList = new ArrayList<>();


        int e = buttonListWidget.getEntryAt(mouseX, mouseY);
        if (e > -1) {
            ButtonWidget element = buttonListWidget.getEntry(e).getButtonWidget();
            if (tooltips.containsKey(element)) {
                String text = tooltips.get(element).get();
                tooltipList.addAll(Arrays.asList(text.split("\n")));
                return tooltipList;
            }
        }


        if (SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED) {
            tooltipList.add(Language.getInstance().translate("speedrunigt.message.update_found"));
        }
        return tooltipList;
    }


    public void selectCategory(String key) {
        if (categorySelectButtons.containsKey(key) && categorySubButtons.containsKey(key)) {
            if (categorySelectButtons.containsKey(currentSelectCategory)) categorySelectButtons.get(currentSelectCategory).active = true;
            currentSelectCategory = key;

            categorySelectButtons.get(key).active = false;
            buttonListWidget.replaceButtons(categorySubButtons.get(key));
            buttonListWidget.scroll(0);
        }
    }

    class ButtonScrollListWidget extends EntryListWidget {

        public ButtonScrollListWidget() {
            super(SpeedRunOptionScreen.this.field_1229, SpeedRunOptionScreen.this.width - 140, SpeedRunOptionScreen.this.height, 28, SpeedRunOptionScreen.this.height - 54, 24);
        }

        public void replaceButtons(Collection<ButtonWidget> buttonWidgets) {
            widgetButtons.clear();
            ArrayList<ButtonScrollListEntry> list = new ArrayList<>();
            for (ButtonWidget buttonWidget : buttonWidgets) {
                widgetButtons.add(buttonWidget);
                list.add(new ButtonScrollListEntry(buttonWidget));
            }
            entries.clear();
            entries.addAll(list);
        }

        @Override
        public int getRowWidth() {
            return 150;
        }

        private final ArrayList<ButtonScrollListEntry> entries = new ArrayList<>();

        @Override
        public ButtonScrollListEntry getEntry(int i) {
            return entries.get(i);
        }

        @Override
        protected int getEntryCount() {
            return entries.size();
        }

        @Override
        public void render(int mouseX, int mouseY, float delta) {
            super.render(mouseX, mouseY, delta);

            //Render bg on empty space
            if (SpeedRunOptionScreen.this.field_1229 == null) return;
            int emptyWidth = this.width;
            GL11.glDisable(2896);
            GL11.glDisable(2912);
            Tessellator var2 = Tessellator.INSTANCE;
            SpeedRunOptionScreen.this.field_1229.textureManager.bindTexture(SpeedRunOptionScreen.this.field_1229.textureManager.getTextureFromPath("/gui/background.png"));
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            float var3 = 32.0F;
            var2.begin();
            var2.color(4210752);
            var2.vertex(emptyWidth, SpeedRunOptionScreen.this.height, 0.0D, emptyWidth / var3, ((float)SpeedRunOptionScreen.this.height / var3));
            var2.vertex(SpeedRunOptionScreen.this.width, SpeedRunOptionScreen.this.height, 0.0D, ((float)SpeedRunOptionScreen.this.width / var3), (float)SpeedRunOptionScreen.this.height / var3);
            var2.vertex(SpeedRunOptionScreen.this.width, 0.0D, 0.0D, ((float)SpeedRunOptionScreen.this.width / var3), 0);
            var2.vertex(emptyWidth, 0.0D, 0.0D, emptyWidth / var3, 0);
            var2.end();
        }

        class ButtonScrollListEntry implements EntryWidget {
            private final ButtonWidget buttonWidget;

            public ButtonScrollListEntry(ButtonWidget buttonWidget) {
                this.buttonWidget = buttonWidget;
                this.buttonWidget.x = (ButtonScrollListWidget.this.width - ((ButtonWidgetAccessor)this.buttonWidget).getWidth()) / 2;
            }

            public ButtonWidget getButtonWidget() {
                return buttonWidget;
            }

            @Override
            public void draw(int index, int x, int y, int rowWidth, int rowHeight, Tessellator tessellator, int mouseX, int mouseY, boolean hovered) {
                buttonWidget.y = y;
                buttonWidget.method_891(SpeedRunOptionScreen.this.field_1229, mouseX, mouseY);
            }

            @Override
            public boolean mouseClicked(int i, int j, int k, int l, int m, int n) {
                return false;
            }

            @Override
            public void mouseReleased(int i, int j, int k, int l, int m, int n) {

            }
        }
    }

}
