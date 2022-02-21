package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.gui.ButtonScrollListWidget;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
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

        addButton(new ButtonWidget(width - 130, height - 30, 100, 20, ScreenTexts.CANCEL, (ButtonWidget button) -> onClose()));

        buttonListWidget = addChild(new ButtonScrollListWidget(client, width - 140, height, 28, height - 54, 24));

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
        fill(matrices, width - 115, 28, width - 25, height - 54, 1677721600);
        super.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215);

        ArrayList<Text> tooltip = getToolTip(mouseX, mouseY);
        if (!tooltip.isEmpty()) this.renderTooltip(matrices, tooltip, 0, height - 34);
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
}
