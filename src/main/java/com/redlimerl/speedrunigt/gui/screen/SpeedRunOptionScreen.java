package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.utils.ButtonWidgetHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Supplier;

public class SpeedRunOptionScreen extends Screen {

    private final Screen parent;
    private final HashMap<String, ArrayList<AbstractWidget>> categorySubButtons = new HashMap<>();
    private final LinkedHashMap<String, AbstractWidget> categorySelectButtons = new LinkedHashMap<>();
    private final HashMap<GuiEventListener, Supplier<String>> tooltips = new HashMap<>();
    private ButtonScrollListWidget buttonListWidget;
    private String currentSelectCategory = "";
    private int page = 0;
    private AbstractWidget prevPageButton = null;
    private AbstractWidget nextPageButton = null;

    public SpeedRunOptionScreen(Screen parent) {
        super(Component.translatable("speedrunigt.title.options"));
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
            AbstractWidget button = builder.getButtonWidget();
            if (builder.getTooltip() != null) tooltips.put(button, builder.getTooltip());

            String category = builder.getCategory();
            ArrayList<AbstractWidget> categoryList = categorySubButtons.getOrDefault(category, new ArrayList<>());
            categoryList.add(button);
            categorySubButtons.put(category, categoryList);

            if (!categorySelectButtons.containsKey(category)) {
                Button buttonWidget = ButtonWidgetHelper.create(width - 110, 30 + ((categoryCount++ % 6) * 22), 80, 20, Component.translatable(category), (Button buttonWidget1) -> selectCategory(category));
                categorySelectButtons.put(category, buttonWidget);
                addRenderableWidget(buttonWidget);
            }
        }

        prevPageButton = addRenderableWidget(ButtonWidgetHelper.create(width - 110, 30 + (6 * 22), 38, 20, Component.literal("<"), (Button button) -> openPage(-1)));

        nextPageButton = addRenderableWidget(ButtonWidgetHelper.create(width - 68, 30 + (6 * 22), 38, 20, Component.literal(">"), (Button button) -> openPage(+1)));

        openPage(page);

        addRenderableWidget(ButtonWidgetHelper.create(width - 85, height - 35, 70, 20, CommonComponents.GUI_CANCEL, (Button button) -> onClose()));

        addRenderableWidget(ButtonWidgetHelper.create(15, height - 35, 70, 20, Component.translatable("speedrunigt.menu.donate"), (Button button) -> Util.getPlatform().openUri("https://ko-fi.com/redlimerl")));

        addRenderableWidget(ButtonWidgetHelper.create(88, height - 35, 140, 20, Component.translatable("speedrunigt.menu.crowdin"), (Button button) -> Util.getPlatform().openUri("https://crowdin.com/project/speedrunigt")));

        buttonListWidget = addWidget(new ButtonScrollListWidget());

        if (!currentSelectCategory.isEmpty()) selectCategory(currentSelectCategory);
        else categorySelectButtons.keySet().stream().findFirst().ifPresent(this::selectCategory);
    }

    public void openPage(int num) {
        int maxPage = Math.max((categorySelectButtons.keySet().size() - 1) / 6, 0);
        this.page = Mth.clamp(this.page + num, 0, maxPage);

        int count = 0;
        for (AbstractWidget value : categorySelectButtons.values()) {
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
    public void onClose() {
        if (this.minecraft != null) this.minecraft.gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        this.buttonListWidget.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, this.title, this.width / 2, 10, CommonColors.WHITE);
        graphics.text(font, "v"+ SpeedRunIGT.MOD_VERSION, 4, 4, CommonColors.WHITE, true);

        ArrayList<Component> tooltip = getToolTip(mouseX, mouseY);
        if (!tooltip.isEmpty() && !this.isDragging()) graphics.setComponentTooltipForNextFrame(font, tooltip, 0, height);
    }

    public ArrayList<Component> getToolTip(int mouseX, int mouseY) {
        ArrayList<Component> tooltipList = new ArrayList<>();

        Optional<GuiEventListener> e = buttonListWidget.getChildAt(mouseX, mouseY);
        if (e.isPresent()) {
            GuiEventListener element = e.get();
            if (element instanceof ButtonScrollListWidget.Entry entry) {
                AbstractWidget buttonWidget = entry.getButtonWidget();
                if (tooltips.containsKey(buttonWidget)) {
                    String text = tooltips.get(buttonWidget).get();
                    for (String s : text.split("\n")) {
                        tooltipList.add(Component.literal(s));
                    }
                    return tooltipList;
                }
            }
        }

        if (SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED) {
            tooltipList.add(Component.translatable("speedrunigt.message.update_found"));
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

    class ButtonScrollListWidget extends ContainerObjectSelectionList<ButtonScrollListWidget.Entry> {

        public ButtonScrollListWidget() {
            super(SpeedRunOptionScreen.this.minecraft, SpeedRunOptionScreen.this.width - 140, SpeedRunOptionScreen.this.height - 82, 28, 24);
        }

        public void replaceButtons(Collection<AbstractWidget> buttonWidgets) {
            ArrayList<Entry> list = new ArrayList<>();
            for (AbstractWidget buttonWidget : buttonWidgets) {
                list.add(new Entry(buttonWidget));
            }
            replaceEntries(list);
        }

        @Override
        public int getRowWidth() {
            return 150;
        }

        class Entry extends ContainerObjectSelectionList.Entry<Entry> {
            ArrayList<AbstractWidget> children = new ArrayList<>();
            private final AbstractWidget buttonWidget;

            public Entry(AbstractWidget buttonWidget) {
                this.buttonWidget = buttonWidget;
                this.buttonWidget.setX((ButtonScrollListWidget.this.width - this.buttonWidget.getWidth()) / 2);
                children.add(this.buttonWidget);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return children;
            }

            public AbstractWidget getButtonWidget() {
                return buttonWidget;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
                buttonWidget.setY(this.getY());
                buttonWidget.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
            }
        }
    }

}
