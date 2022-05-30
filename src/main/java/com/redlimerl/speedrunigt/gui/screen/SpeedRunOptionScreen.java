package com.redlimerl.speedrunigt.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

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
        super();
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
                ButtonWidget buttonWidget = new ConsumerButtonWidget(width - 110, 30 + ((categoryCount++ % 6) * 22), 80, 20, new TranslatableText(category).asFormattedString(), (buttonWidget1) -> selectCategory(category));
                categorySelectButtons.put(category, buttonWidget);
                method_13411(buttonWidget);
            }
        }

        prevPageButton = method_13411(new ConsumerButtonWidget(width - 110, 30 + (6 * 22), 38, 20, "<", (button) -> openPage(-1)));

        nextPageButton = method_13411(new ConsumerButtonWidget(width - 68, 30 + (6 * 22), 38, 20, ">", (button) -> openPage(+1)));

        openPage(0);

        method_13411(new ConsumerButtonWidget(width - 85, height - 35, 70, 20, ScreenTexts.CANCEL, (button) -> method_18608()));

        method_13411(new ConsumerButtonWidget(15, height - 35, 70, 20, new TranslatableText("speedrunigt.menu.donate").asFormattedString(), (button) -> Util.getOperatingSystem().method_20236("https://ko-fi.com/redlimerl")));

        buttonListWidget = new ButtonScrollListWidget();

        categorySelectButtons.keySet().stream().findFirst().ifPresent(this::selectCategory);
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

    @Override
    public void method_18608() {
        if (this.client != null) this.client.openScreen(parent);
    }

    @Override
    public boolean mouseScrolled(double d) {
        return this.buttonListWidget.mouseScrolled(d);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        ArrayList<ButtonWidget> widgets = new ArrayList<>(widgetButtons);
        buttons.addAll(widgets);
        boolean c = super.mouseClicked(d, e, i);
        buttons.removeAll(widgets);
        return c;
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        this.buttonListWidget.render(mouseX, mouseY, delta);
        super.render(mouseX, mouseY, delta);
        drawCenteredString(this.textRenderer, new TranslatableText("speedrunigt.title.options").asFormattedString(), this.width / 2, 10, 16777215);
        this.textRenderer.drawWithShadow("v"+ SpeedRunIGT.MOD_VERSION, 4, 4, 16777215);

        ArrayList<String> tooltip = getToolTip();
        if (!tooltip.isEmpty()) this.renderTooltip(tooltip, 0, height);
    }

    public ArrayList<String> getToolTip() {
        ArrayList<String> tooltipList = new ArrayList<>();

        for (ButtonScrollListWidget.ButtonScrollListEntry entry : buttonListWidget.entries) {
            if (entry.buttonWidget.isHovered()) {
                if (tooltips.containsKey(entry.buttonWidget)) {
                    String text = tooltips.get(entry.buttonWidget).get();
                    tooltipList.addAll(Arrays.asList(text.split("\n")));
                    return tooltipList;
                }
            }
        }

        if (SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED) {
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
            buttonListWidget.scroll(0);
        }
    }

    class ButtonScrollListWidget extends EntryListWidget<ButtonScrollListWidget.ButtonScrollListEntry> {

        public ButtonScrollListWidget() {
            super(SpeedRunOptionScreen.this.client, SpeedRunOptionScreen.this.width - 140, SpeedRunOptionScreen.this.height, 28, SpeedRunOptionScreen.this.height - 54, 24);
        }

        public void replaceButtons(Collection<ButtonWidget> buttonWidgets) {
            SpeedRunOptionScreen.this.field_20307.removeAll(widgetButtons);
            widgetButtons.clear();
            this.method_18423().clear();
            ArrayList<ButtonScrollListEntry> list = new ArrayList<>();
            for (ButtonWidget buttonWidget : buttonWidgets) {
                SpeedRunOptionScreen.this.field_20307.add(buttonWidget);
                widgetButtons.add(buttonWidget);
                ButtonScrollListEntry entry = new ButtonScrollListEntry(buttonWidget);
                this.method_18423().add(entry);
                list.add(entry);
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
        public void render(int mouseX, int mouseY, float delta) {
            if (this.client == null) return;
            super.render(mouseX, mouseY, delta);

            //Render bg on empty space
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            this.client.getTextureManager().bindTexture(OPTIONS_BACKGROUND_TEXTURE);
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

        class ButtonScrollListEntry extends EntryListWidget.Entry<ButtonScrollListEntry> {
            private final ButtonWidget buttonWidget;

            public ButtonScrollListEntry(ButtonWidget buttonWidget) {
                this.buttonWidget = buttonWidget;
                this.buttonWidget.x = (ButtonScrollListWidget.this.width - this.buttonWidget.getWidth()) / 2;
            }

            @Override
            public void method_6700(int i, int j, int k, int l, boolean bl, float f) {
                int n = this.method_18404();
                int m = this.method_18403();
                buttonWidget.x = n;
                buttonWidget.y = m;
                buttonWidget.method_891(k, l, f);
            }
        }
    }

}
