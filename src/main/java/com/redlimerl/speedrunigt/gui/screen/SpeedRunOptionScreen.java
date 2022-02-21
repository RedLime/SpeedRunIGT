package com.redlimerl.speedrunigt.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
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

import java.util.*;
import java.util.function.Supplier;

public class SpeedRunOptionScreen extends Screen {

    private final Screen parent;
    private final HashMap<String, ArrayList<AbstractButtonWidget>> categorySubButtons = new HashMap<>();
    private final LinkedHashMap<String, AbstractButtonWidget> categorySelectButtons = new LinkedHashMap<>();
    private final HashMap<Element, Supplier<String>> tooltips = new HashMap<>();
    private ButtonScrollListWidget buttonListWidget;
    private String currentSelectCategory = "";

    public SpeedRunOptionScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.title.options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        SpeedRunIGTInfoScreen.checkUpdate();
        categorySubButtons.clear();
        categorySelectButtons.clear();
        tooltips.clear();

        List<OptionButtonFactory> optionButtonFactoryList = SpeedRunOption.getOptionButtonFactories();

        int categoryCount = 0;

        for (OptionButtonFactory factory : optionButtonFactoryList) {
            OptionButtonFactory.Storage builder = factory.create(this).build();
            AbstractButtonWidget button = builder.getButtonWidget();
            if (builder.getTooltip() != null) tooltips.put(button, builder.getTooltip());

            String category = builder.getCategory();
            ArrayList<AbstractButtonWidget> categoryList = categorySubButtons.getOrDefault(category, new ArrayList<>());
            categoryList.add(button);
            categorySubButtons.put(category, categoryList);

            if (!categorySelectButtons.containsKey(category)) {
                ButtonWidget buttonWidget = new ButtonWidget(width - 110, 30 + (categoryCount++ * 22), 80, 20, new TranslatableText(category), (ButtonWidget buttonWidget1) -> selectCategory(category));
                categorySelectButtons.put(category, buttonWidget);
                addButton(buttonWidget);
            }
        }

        addButton(new ButtonWidget(width - 115, height - 35, 100, 20, ScreenTexts.CANCEL, (ButtonWidget button) -> onClose()));

        buttonListWidget = addChild(new ButtonScrollListWidget());

        categorySubButtons.keySet().stream().findFirst().ifPresent(this::selectCategory);
    }

    @Override
    public void onClose() {
        if (this.client != null) this.client.openScreen(parent);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.buttonListWidget.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215);

        ArrayList<Text> tooltip = getToolTip(mouseX, mouseY);
        if (!tooltip.isEmpty()) this.renderTooltip(matrices, tooltip, 0, height);
    }

    public ArrayList<Text> getToolTip(int mouseX, int mouseY) {
        ArrayList<Text> tooltipList = new ArrayList<>();

        Optional<Element> e = buttonListWidget.hoveredElement(mouseX, mouseY);
        if (e.isPresent()) {
            Element element = e.get();
            if (element instanceof ButtonScrollListWidget.Entry) {
                ButtonScrollListWidget.Entry entry = (ButtonScrollListWidget.Entry) element;
                AbstractButtonWidget buttonWidget = entry.getButtonWidget();
                if (tooltips.containsKey(buttonWidget)) {
                    String text = tooltips.get(buttonWidget).get();
                    for (String s : text.split("\n")) {
                        tooltipList.add(new LiteralText(s));
                    }
                    return tooltipList;
                }
            }
        }

        if (SpeedRunIGTInfoScreen.UPDATE_STATUS == SpeedRunIGTInfoScreen.UpdateStatus.OUTDATED) {
            tooltipList.add(new TranslatableText("speedrunigt.message.update_found"));
        }
        return tooltipList;
    }


    public void selectCategory(String key) {
        if (categorySelectButtons.containsKey(key) && categorySubButtons.containsKey(key)) {
            if (categorySelectButtons.containsKey(currentSelectCategory)) categorySelectButtons.get(currentSelectCategory).active = true;
            currentSelectCategory = key;

            categorySelectButtons.get(key).active = false;
            buttonListWidget.replaceButtons(categorySubButtons.get(key));
            buttonListWidget.setScrollAmount(0);
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
                children.add(this.buttonWidget);
            }

            @Override
            public List<? extends Element> children() {
                return children;
            }

            public AbstractButtonWidget getButtonWidget() {
                return buttonWidget;
            }

            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                buttonWidget.y = y;
                buttonWidget.render(matrices, mouseX, mouseY, tickDelta);
            }
        }
    }

}
