package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.function.Supplier;

public class SpeedRunOptionScreen extends Screen {

    private final Screen parent;
    private final HashMap<String, ArrayList<ClickableWidget>> categorySubButtons = new HashMap<>();
    private final LinkedHashMap<String, ClickableWidget> categorySelectButtons = new LinkedHashMap<>();
    private final HashMap<Element, Supplier<String>> tooltips = new HashMap<>();
    private ButtonScrollListWidget buttonListWidget;
    private String currentSelectCategory = "";
    private int page = 0;
    private ClickableWidget prevPageButton = null;
    private ClickableWidget nextPageButton = null;

    public SpeedRunOptionScreen(Screen parent) {
        super(Text.translatable("speedrunigt.title.options"));
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
            ClickableWidget button = builder.getButtonWidget();
            if (builder.getTooltip() != null) tooltips.put(button, builder.getTooltip());

            String category = builder.getCategory();
            ArrayList<ClickableWidget> categoryList = categorySubButtons.getOrDefault(category, new ArrayList<>());
            categoryList.add(button);
            categorySubButtons.put(category, categoryList);

            if (!categorySelectButtons.containsKey(category)) {
                ButtonWidget buttonWidget = ButtonWidgetHelper.create(width - 110, 30 + ((categoryCount++ % 6) * 22), 80, 20, Text.translatable(category), (ButtonWidget buttonWidget1) -> selectCategory(category));
                categorySelectButtons.put(category, buttonWidget);
                addDrawableChild(buttonWidget);
            }
        }

        prevPageButton = addDrawableChild(ButtonWidgetHelper.create(width - 110, 30 + (6 * 22), 38, 20, Text.literal("<"), (ButtonWidget button) -> openPage(-1)));

        nextPageButton = addDrawableChild(ButtonWidgetHelper.create(width - 68, 30 + (6 * 22), 38, 20, Text.literal(">"), (ButtonWidget button) -> openPage(+1)));

        openPage(page);

        addDrawableChild(ButtonWidgetHelper.create(width - 85, height - 35, 70, 20, ScreenTexts.CANCEL, (ButtonWidget button) -> close()));

        addDrawableChild(ButtonWidgetHelper.create(15, height - 35, 70, 20, Text.translatable("speedrunigt.menu.donate"), (ButtonWidget button) -> Util.getOperatingSystem().open("https://ko-fi.com/redlimerl")));

        addDrawableChild(ButtonWidgetHelper.create(88, height - 35, 140, 20, Text.translatable("speedrunigt.menu.crowdin"), (ButtonWidget button) -> Util.getOperatingSystem().open("https://crowdin.com/project/speedrunigt")));

        buttonListWidget = addSelectableChild(new ButtonScrollListWidget());

        if (!currentSelectCategory.isEmpty()) selectCategory(currentSelectCategory);
        else categorySelectButtons.keySet().stream().findFirst().ifPresent(this::selectCategory);
    }

    public void openPage(int num) {
        int maxPage = Math.max((categorySelectButtons.keySet().size() - 1) / 6, 0);
        this.page = MathHelper.clamp(this.page + num, 0, maxPage);

        int count = 0;
        for (ClickableWidget value : categorySelectButtons.values()) {
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

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.buttonListWidget.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 10, Colors.WHITE);
        context.drawText(textRenderer, "v"+ SpeedRunIGT.MOD_VERSION, 4, 4, Colors.WHITE, true);

        ArrayList<Text> tooltip = getToolTip(mouseX, mouseY);
        if (!tooltip.isEmpty() && !this.isDragging()) context.drawTooltip(textRenderer, tooltip, 0, height);
    }

    public ArrayList<Text> getToolTip(int mouseX, int mouseY) {
        ArrayList<Text> tooltipList = new ArrayList<>();

        Optional<Element> e = buttonListWidget.hoveredElement(mouseX, mouseY);
        if (e.isPresent()) {
            Element element = e.get();
            if (element instanceof ButtonScrollListWidget.Entry entry) {
                ClickableWidget buttonWidget = entry.getButtonWidget();
                if (tooltips.containsKey(buttonWidget)) {
                    String text = tooltips.get(buttonWidget).get();
                    for (String s : text.split("\n")) {
                        tooltipList.add(Text.literal(s));
                    }
                    return tooltipList;
                }
            }
        }

        if (SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED) {
            tooltipList.add(Text.translatable("speedrunigt.message.update_found"));
        }
        return tooltipList;
    }


    public void selectCategory(String key) {
        if (categorySelectButtons.containsKey(key) && categorySubButtons.containsKey(key)) {
            if (categorySelectButtons.containsKey(currentSelectCategory)) categorySelectButtons.get(currentSelectCategory).active = true;
            currentSelectCategory = key;

            categorySelectButtons.get(key).active = false;
            buttonListWidget.replaceButtons(categorySubButtons.get(key));
            buttonListWidget.setScrollY(0);
        }
    }

    class ButtonScrollListWidget extends ElementListWidget<ButtonScrollListWidget.Entry> {

        public ButtonScrollListWidget() {
            super(SpeedRunOptionScreen.this.client, SpeedRunOptionScreen.this.width - 140, SpeedRunOptionScreen.this.height - 82, 28, 24);
        }

        public void replaceButtons(Collection<ClickableWidget> buttonWidgets) {
            ArrayList<Entry> list = new ArrayList<>();
            for (ClickableWidget buttonWidget : buttonWidgets) {
                list.add(new Entry(buttonWidget));
            }
            replaceEntries(list);
        }

        @Override
        public int getRowWidth() {
            return 150;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
        }

        class Entry extends ElementListWidget.Entry<Entry> {
            ArrayList<ClickableWidget> children = new ArrayList<>();
            private final ClickableWidget buttonWidget;

            public Entry(ClickableWidget buttonWidget) {
                this.buttonWidget = buttonWidget;
                this.buttonWidget.setX((ButtonScrollListWidget.this.width - this.buttonWidget.getWidth()) / 2);
                children.add(this.buttonWidget);
            }

            @Override
            public List<? extends Element> children() {
                return children;
            }

            @Override
            public List<? extends Selectable> selectableChildren() {
                return children;
            }

            public ClickableWidget getButtonWidget() {
                return buttonWidget;
            }

            @Override
            public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
                buttonWidget.setY(this.getY());
                buttonWidget.render(context, mouseX, mouseY, deltaTicks);
            }
        }
    }

}
