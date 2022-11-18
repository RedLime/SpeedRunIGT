package com.redlimerl.speedrunigt.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.function.Supplier;

public class SpeedRunOptionScreen extends Screen {
    private final Screen parent;
    private final HashMap<String, ArrayList<AbstractButtonWidget>> categorySubButtons = new HashMap<>();
    private final LinkedHashMap<String, AbstractButtonWidget> categorySelectButtons = new LinkedHashMap<>();
    private final HashMap<Element, Supplier<String>> tooltips = new HashMap<>();
    private ButtonScrollListWidget buttonListWidget;
    private String currentSelectCategory = "";
    private int page = 0;
    private AbstractButtonWidget prevPageButton = null;
    private AbstractButtonWidget nextPageButton = null;

    public SpeedRunOptionScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.title.options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.categorySubButtons.clear();
        this.categorySelectButtons.clear();
        this.tooltips.clear();

        List<OptionButtonFactory> optionButtonFactoryList = SpeedRunOption.getOptionButtonFactories();

        int categoryCount = 0;

        for (OptionButtonFactory factory : optionButtonFactoryList) {
            OptionButtonFactory.Storage builder = factory.create(this).build();
            AbstractButtonWidget button = builder.getButtonWidget();
            if (builder.getTooltip() != null) this.tooltips.put(button, builder.getTooltip());

            String category = builder.getCategory();
            ArrayList<AbstractButtonWidget> categoryList = this.categorySubButtons.getOrDefault(category, new ArrayList<>());
            categoryList.add(button);
            this.categorySubButtons.put(category, categoryList);

            if (!this.categorySelectButtons.containsKey(category)) {
                ButtonWidget buttonWidget = new ButtonWidget(this.width - 110, 30 + ((categoryCount++ % 6) * 22), 80, 20, new TranslatableText(category), (ButtonWidget buttonWidget1) -> selectCategory(category));
                this.categorySelectButtons.put(category, buttonWidget);
                this.addButton(buttonWidget);
            }
        }

        this.prevPageButton = this.addButton(new ButtonWidget(this.width - 110, 30 + (6 * 22), 38, 20, new LiteralText("<"), (ButtonWidget button) -> openPage(-1)));
        this.nextPageButton = this.addButton(new ButtonWidget(this.width - 68, 30 + (6 * 22), 38, 20, new LiteralText(">"), (ButtonWidget button) -> openPage(+1)));

        openPage(page);

        this.addButton(new ButtonWidget(this.width - 85, this.height - 35, 70, 20, ScreenTexts.CANCEL, (ButtonWidget button) -> onClose()));
        this.addButton(new ButtonWidget(15, this.height - 35, 70, 20, new TranslatableText("speedrunigt.menu.donate"), (ButtonWidget button) -> Util.getOperatingSystem().open("https://ko-fi.com/redlimerl")));
        this.addButton(new ButtonWidget(88, this.height - 35, 140, 20, new TranslatableText("speedrunigt.menu.crowdin"), (ButtonWidget button) -> Util.getOperatingSystem().open("https://crowdin.com/project/speedrunigt")));

        this.buttonListWidget = addChild(new ButtonScrollListWidget());

        if (!this.currentSelectCategory.isEmpty()) selectCategory(this.currentSelectCategory);
        else this.categorySelectButtons.keySet().stream().findFirst().ifPresent(this::selectCategory);
    }

    public void openPage(int num) {
        int maxPage = Math.max((this.categorySelectButtons.keySet().size() - 1) / 6, 0);
        this.page = MathHelper.clamp(this.page + num, 0, maxPage);

        int count = 0;
        for (AbstractButtonWidget value : this.categorySelectButtons.values()) {
            value.visible = this.page * 6 <= count && (this.page + 1) * 6 > count;
            count++;
        }

        if (maxPage == 0) {
            this.prevPageButton.visible = false;
            this.nextPageButton.visible = false;
        } else {
            this.prevPageButton.visible = true;
            this.nextPageButton.visible = true;
            this.prevPageButton.active = !(this.page == 0);
            this.nextPageButton.active = !(maxPage == this.page);
        }
    }

    @Override
    public void onClose() {
        if (this.client != null) this.client.openScreen(this.parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        
        this.buttonListWidget.render(matrices, mouseX, mouseY, delta);
        
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215);
        this.drawStringWithShadow(matrices, this.textRenderer, "v"+ SpeedRunIGT.MOD_VERSION, 4, 4, 16777215);

        ArrayList<Text> tooltip = getToolTip(mouseX, mouseY);
        if (!tooltip.isEmpty() && !this.isDragging()) this.renderTooltip(matrices, tooltip, 0, this.height);

        super.render(matrices, mouseX, mouseY, delta);
    }

    public ArrayList<Text> getToolTip(int mouseX, int mouseY) {
        ArrayList<Text> tooltipList = new ArrayList<>();

        Optional<Element> e = this.buttonListWidget.hoveredElement(mouseX, mouseY);
        if (e.isPresent()) {
            Element element = e.get();
            if (element instanceof ButtonScrollListWidget.Entry) {
                ButtonScrollListWidget.Entry entry = (ButtonScrollListWidget.Entry) element;
                AbstractButtonWidget buttonWidget = entry.getButtonWidget();
                if (this.tooltips.containsKey(buttonWidget)) {
                    String text = this.tooltips.get(buttonWidget).get();
                    for (String s : text.split("\n")) {
                        tooltipList.add(new LiteralText(s));
                    }
                    return tooltipList;
                }
            }
        }

        if (SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED) {
            tooltipList.add(new TranslatableText("speedrunigt.message.update_found"));
        }
        return tooltipList;
    }
    
    public void selectCategory(String key) {
        if (this.categorySelectButtons.containsKey(key) && this.categorySubButtons.containsKey(key)) {
            if (this.categorySelectButtons.containsKey(this.currentSelectCategory)) this.categorySelectButtons.get(this.currentSelectCategory).active = true;
            this.currentSelectCategory = key;

            this.categorySelectButtons.get(key).active = false;
            this.buttonListWidget.replaceButtons(this.categorySubButtons.get(key));
            this.buttonListWidget.setScrollAmount(0);
        }
    }

    class ButtonScrollListWidget extends ElementListWidget<ButtonScrollListWidget.Entry> {
        public ButtonScrollListWidget() {
            super(SpeedRunOptionScreen.this.client, SpeedRunOptionScreen.this.width - 140, SpeedRunOptionScreen.this.height, 28, SpeedRunOptionScreen.this.height - 54, 24);
        }

        public void replaceButtons(Collection<AbstractButtonWidget> buttonWidgets) {
            ArrayList<Entry> list = new ArrayList<>();
            for (AbstractButtonWidget buttonWidget : buttonWidgets) {
                list.add(new Entry(buttonWidget));
            }
            replaceEntries(list);
        }

        @Override
        public int getRowWidth() {
            return 150;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);

            //Render bg on empty space
            if (this.client == null) return;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            this.client.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            int emptyWidth = this.width;
            bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(this.width, this.height, 0.0D).texture(emptyWidth / f, (float)this.height / f).color(64, 64, 64, 255).next();
            bufferBuilder.vertex(SpeedRunOptionScreen.this.width, this.height, 0.0D).texture((float)SpeedRunOptionScreen.this.width / f, (float)this.height / f + 0).color(64, 64, 64, 255).next();
            bufferBuilder.vertex(SpeedRunOptionScreen.this.width, 0.0D, 0.0D).texture((float)SpeedRunOptionScreen.this.width / f, 0).color(64, 64, 64, 255).next();
            bufferBuilder.vertex(this.width, 0.0D, 0.0D).texture(emptyWidth / f, 0).color(64, 64, 64, 255).next();
            tessellator.draw();
        }

        class Entry extends ElementListWidget.Entry<Entry> {
            ArrayList<AbstractButtonWidget> children = new ArrayList<>();
            private final AbstractButtonWidget buttonWidget;

            public Entry(AbstractButtonWidget buttonWidget) {
                this.buttonWidget = buttonWidget;
                this.buttonWidget.x = (ButtonScrollListWidget.this.width - this.buttonWidget.getWidth()) / 2;
                this.children.add(this.buttonWidget);
            }

            @Override
            public List<? extends Element> children() {
                return this.children;
            }

            public AbstractButtonWidget getButtonWidget() {
                return this.buttonWidget;
            }

            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                this.buttonWidget.y = y;
                this.buttonWidget.render(matrices, mouseX, mouseY, tickDelta);
            }
        }
    }
}
