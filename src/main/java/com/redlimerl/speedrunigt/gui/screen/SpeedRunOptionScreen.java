package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.utils.OperatingUtils;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.class_1015;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.function.Supplier;

public class SpeedRunOptionScreen extends Screen {

    private final Screen parent;
    private final HashMap<String, ArrayList<ClickableWidget>> categorySubButtons = new HashMap<>();
    private final LinkedHashMap<String, ClickableWidget> categorySelectButtons = new LinkedHashMap<>();
    private final HashMap<ClickableWidget, Supplier<String>> tooltips = new HashMap<>();
    private ButtonScrollListWidget buttonListWidget;
    private final ArrayList<ClickableWidget> widgetButtons = new ArrayList<>();
    private String currentSelectCategory = "";
    private int page = 0;
    private ClickableWidget prevPageButton = null;
    private ClickableWidget nextPageButton = null;

    public SpeedRunOptionScreen(Screen parent) {
        super();
        this.parent = parent;
    }

    @Override
    public void method_2224() {
        super.method_2224();
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
                ClickableWidget buttonWidget = new ConsumerButtonWidget(field_2561 - 110, 30 + ((categoryCount++ % 6) * 22), 80, 20, new TranslatableTextContent(category).method_10865(), (buttonWidget1) -> selectCategory(category));
                categorySelectButtons.put(category, buttonWidget);
                field_2564.add(buttonWidget);
            }
        }

        prevPageButton = method_2219(new ConsumerButtonWidget(field_2561 - 110, 30 + (6 * 22), 38, 20, "<", (button) -> openPage(-1)));

        nextPageButton = method_2219(new ConsumerButtonWidget(field_2561 - 68, 30 + (6 * 22), 38, 20, ">", (button) -> openPage(+1)));

        openPage(page);

        field_2564.add(new ConsumerButtonWidget(field_2561 - 85, field_2559 - 35, 70, 20, ScreenTexts.CANCEL, (button) -> onClose()));

        field_2564.add(new ConsumerButtonWidget(15, field_2559 - 35, 70, 20, new TranslatableTextContent("speedrunigt.menu.donate").method_10865(), (button) -> OperatingUtils.setUrl("https://ko-fi.com/redlimerl")));

        method_2219(new ConsumerButtonWidget(88, field_2559 - 35, 140, 20, new TranslatableTextContent("speedrunigt.menu.crowdin").method_10865(), (button) -> OperatingUtils.setUrl("https://crowdin.com/project/speedrunigt")));

        buttonListWidget = new ButtonScrollListWidget();

        if (!currentSelectCategory.isEmpty()) selectCategory(currentSelectCategory);
        else categorySelectButtons.keySet().stream().findFirst().ifPresent(this::selectCategory);
    }

    public void openPage(int num) {
        int maxPage = Math.max((categorySelectButtons.keySet().size() - 1) / 6, 0);
        this.page = MathHelper.clamp(this.page + num, 0, maxPage);

        int count = 0;
        for (ClickableWidget value : categorySelectButtons.values()) {
            value.field_2076 = this.page * 6 <= count && (this.page + 1) * 6 > count;
            count++;
        }

        if (maxPage == 0) {
            prevPageButton.field_2076 = false;
            nextPageButton.field_2076 = false;
        } else {
            prevPageButton.field_2076 = true;
            nextPageButton.field_2076 = true;
            prevPageButton.field_2078 = !(this.page == 0);
            nextPageButton.field_2078 = !(maxPage == this.page);
        }
    }

    public void onClose() {
        if (this.field_2563 != null) this.field_2563.setScreen(parent);
    }

    @Override
    protected void method_0_2778(ClickableWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.method_0_2778(button);
    }

    @Override
    public void method_0_2801() {
        super.method_0_2801();
        this.buttonListWidget.method_0_2650();
    }

    @Override
    protected void method_0_2775(int mouseX, int mouseY, int button) {
        ArrayList<ClickableWidget> widgets = new ArrayList<>(widgetButtons);
        field_2564.addAll(widgets);
        super.method_0_2775(mouseX, mouseY, button);
        field_2564.removeAll(widgets);
    }

    @Override
    public void method_2214(int mouseX, int mouseY, float delta) {
        this.method_2240();
        this.buttonListWidget.method_1930(mouseX, mouseY, delta);
        super.method_2214(mouseX, mouseY, delta);
        method_1789(this.field_2554, new TranslatableTextContent("speedrunigt.title.options").method_10865(), this.field_2561 / 2, 10, 16777215);
        this.field_2554.method_0_2382("v"+ SpeedRunIGT.MOD_VERSION, 4, 4, 16777215);

        ArrayList<String> tooltip = getToolTip(mouseX, mouseY);
        if (!tooltip.isEmpty() && this.field_0_3200 == null) this.method_2211(tooltip, 0, field_2559);
    }

    public ArrayList<String> getToolTip(int mouseX, int mouseY) {
        ArrayList<String> tooltipList = new ArrayList<>();


        int e = buttonListWidget.method_1956(mouseX, mouseY);
        if (e > -1) {
            ClickableWidget element = buttonListWidget.method_0_2558(e).getButtonWidget();
            if (tooltips.containsKey(element)) {
                String text = tooltips.get(element).get();
                tooltipList.addAll(Arrays.asList(text.split("\n")));
                return tooltipList;
            }
        }


        if (SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED) {
            tooltipList.add(new TranslatableTextContent("speedrunigt.message.update_found").method_10865());
        }
        return tooltipList;
    }


    public void selectCategory(String key) {
        if (categorySelectButtons.containsKey(key) && categorySubButtons.containsKey(key)) {
            if (categorySelectButtons.containsKey(currentSelectCategory)) categorySelectButtons.get(currentSelectCategory).field_2078 = true;
            currentSelectCategory = key;

            categorySelectButtons.get(key).field_2078 = false;
            buttonListWidget.replaceButtons(categorySubButtons.get(key));
            buttonListWidget.method_1951(0);
        }
    }

    class ButtonScrollListWidget extends EntryListWidget {

        public ButtonScrollListWidget() {
            super(SpeedRunOptionScreen.this.field_2563, SpeedRunOptionScreen.this.field_2561 - 140, SpeedRunOptionScreen.this.field_2559, 28, SpeedRunOptionScreen.this.field_2559 - 54, 24);
        }

        public void replaceButtons(Collection<ClickableWidget> buttonWidgets) {
            widgetButtons.clear();
            ArrayList<ButtonScrollListEntry> list = new ArrayList<>();
            for (ClickableWidget buttonWidget : buttonWidgets) {
                widgetButtons.add(buttonWidget);
                list.add(new ButtonScrollListEntry(buttonWidget));
            }
            entries.clear();
            entries.addAll(list);
        }

        @Override
        public int method_1932() {
            return 150;
        }

        private final ArrayList<ButtonScrollListEntry> entries = new ArrayList<>();
        @Override
        public ButtonScrollListEntry method_0_2558(int index) {
            return entries.get(index);
        }

        @Override
        protected int method_1947() {
            return entries.size();
        }

        @Override
        public void method_1930(int mouseX, int mouseY, float delta) {
            super.method_1930(mouseX, mouseY, delta);

            //Render bg on empty space
            if (this.field_2164 == null) return;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            this.field_2164.getTextureManager().bindTextureInner(field_2051);
            class_1015.method_4381(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            int emptyWidth = this.field_2168;
            bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(this.field_2168, this.field_2167, 0.0D).texture(emptyWidth / f, (float)this.field_2167 / f).color(64, 64, 64, 255).method_1344();
            bufferBuilder.vertex(SpeedRunOptionScreen.this.field_2561, this.field_2167, 0.0D).texture((float)SpeedRunOptionScreen.this.field_2561 / f, (float)this.field_2167 / f + 0).color(64, 64, 64, 255).method_1344();
            bufferBuilder.vertex(SpeedRunOptionScreen.this.field_2561, 0.0D, 0.0D).texture((float)SpeedRunOptionScreen.this.field_2561 / f, 0).color(64, 64, 64, 255).method_1344();
            bufferBuilder.vertex(this.field_2168, 0.0D, 0.0D).texture(emptyWidth / f, 0).color(64, 64, 64, 255).method_1344();
            tessellator.draw();
        }

        class ButtonScrollListEntry implements EntryListWidget.class_0_701 {
            private final ClickableWidget buttonWidget;

            public ButtonScrollListEntry(ClickableWidget buttonWidget) {
                this.buttonWidget = buttonWidget;
                this.buttonWidget.field_2069 = (ButtonScrollListWidget.this.field_2168 - this.buttonWidget.method_1825()) / 2;
            }

            public ClickableWidget getButtonWidget() {
                return buttonWidget;
            }

            @Override
            public void method_1903(int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                buttonWidget.field_2068 = y;
                buttonWidget.method_1824(SpeedRunOptionScreen.this.field_2563, mouseX, mouseY, tickDelta);
            }

            @Override
            public void method_1904(int i, int j, int k, float f) {
            }

            @Override
            public boolean method_0_2562(int index, int mouseX, int mouseY, int button, int x, int y) {
                return false;
            }

            @Override
            public void method_0_2564(int index, int mouseX, int mouseY, int button, int x, int y) {
            }
        }
    }

}
