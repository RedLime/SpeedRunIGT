package com.redlimerl.speedrunigt.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

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
                ButtonWidget buttonWidget = new ButtonWidget(width - 110, 30 + (categoryCount++ * 22), 80, 20, new TranslatableText(category).asFormattedString(), (ButtonWidget buttonWidget1) -> selectCategory(category));
                categorySelectButtons.put(category, buttonWidget);
                addButton(buttonWidget);
            }
        }

        addButton(new ButtonWidget(width - 85, height - 35, 70, 20, ScreenTexts.CANCEL, (ButtonWidget button) -> onClose()));

        addButton(new ButtonWidget(15, height - 35, 70, 20, new TranslatableText("speedrunigt.menu.donate").asFormattedString(), (ButtonWidget button) -> Util.getOperatingSystem().open("https://ko-fi.com/redlimerl")));

        buttonListWidget = new ButtonScrollListWidget();
        children.add(buttonListWidget);

        categorySelectButtons.keySet().stream().findFirst().ifPresent(this::selectCategory);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.openScreen(parent);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        this.buttonListWidget.render(mouseX, mouseY, delta);
        super.render(mouseX, mouseY, delta);
        drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 10, 16777215);
        this.font.drawWithShadow("v"+ SpeedRunIGT.MOD_VERSION, 4, 4, 16777215);

        ArrayList<String> tooltip = getToolTip(mouseX, mouseY);
        if (!tooltip.isEmpty()) this.renderTooltip(tooltip, 0, height);
    }

    public ArrayList<String> getToolTip(int mouseX, int mouseY) {
        ArrayList<String> tooltipList = new ArrayList<>();

        Optional<Element> e = buttonListWidget.hoveredElement(mouseX, mouseY);
        if (e.isPresent()) {
            Element element = e.get();
            if (element instanceof ButtonScrollListWidget.Entry) {
                ButtonScrollListWidget.Entry entry = (ButtonScrollListWidget.Entry) element;
                AbstractButtonWidget buttonWidget = entry.getButtonWidget();
                if (tooltips.containsKey(buttonWidget)) {
                    String text = tooltips.get(buttonWidget).get();
                    tooltipList.addAll(Arrays.asList(text.split("\n")));
                    return tooltipList;
                }
            }
        }

        if (SpeedRunIGTInfoScreen.UPDATE_STATUS == SpeedRunIGTInfoScreen.UpdateStatus.OUTDATED) {
            tooltipList.add(new TranslatableText("speedrunigt.message.update_found").asFormattedString());
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
            super(SpeedRunOptionScreen.this.minecraft, SpeedRunOptionScreen.this.width - 140, SpeedRunOptionScreen.this.height, 28, SpeedRunOptionScreen.this.height - 54, 24);
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

        @Override
        public void render(int mouseX, int mouseY, float delta) {
            super.render(mouseX, mouseY, delta);

            //Render bg on empty space
            if (this.minecraft == null) return;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            this.minecraft.getTextureManager().bindTexture(BACKGROUND_LOCATION);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
            public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                buttonWidget.y = y;
                buttonWidget.render(mouseX, mouseY, tickDelta);
            }
        }
    }

}
