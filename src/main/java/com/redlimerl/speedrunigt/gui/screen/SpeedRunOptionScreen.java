package com.redlimerl.speedrunigt.gui.screen;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker;
import com.redlimerl.speedrunigt.api.OptionButtonFactory;
import com.redlimerl.speedrunigt.gui.ConsumerButtonWidget;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.utils.OperatingUtils;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.class_1803;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.text.TranslatableText;
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

    public SpeedRunOptionScreen(Screen parent) {
        super();
        this.parent = parent;
    }

    @Override
    public void method_21947() {
        super.method_21947();
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
                ButtonWidget buttonWidget = new ConsumerButtonWidget(field_22535 - 110, 30 + (categoryCount++ * 22), 80, 20, new TranslatableText(category).asFormattedString(), (buttonWidget1) -> selectCategory(category));
                categorySelectButtons.put(category, buttonWidget);
                field_22537.add(buttonWidget);
            }
        }

        field_22537.add(new ConsumerButtonWidget(field_22535 - 85, field_22536 - 35, 70, 20, ScreenTexts.CANCEL, (button) -> onClose()));

        field_22537.add(new ConsumerButtonWidget(15, field_22536 - 35, 70, 20, new TranslatableText("speedrunigt.menu.donate").asFormattedString(), (button) -> OperatingUtils.setUrl("https://ko-fi.com/redlimerl")));

        buttonListWidget = new ButtonScrollListWidget();

        categorySelectButtons.keySet().stream().findFirst().ifPresent(this::selectCategory);
    }

    public void onClose() {
        if (this.field_22534 != null) this.field_22534.openScreen(parent);
    }

    @Override
    protected void method_21930(ButtonWidget button) {
        if (button instanceof ConsumerButtonWidget) {
            ((ConsumerButtonWidget) button).onClick();
        }
        super.method_21930(button);
    }

    @Override
    protected void method_21926(int mouseX, int mouseY, int button) {
        ArrayList<ButtonWidget> widgets = new ArrayList<>(widgetButtons);
        field_22537.addAll(widgets);
        super.method_21926(mouseX, mouseY, button);
        field_22537.removeAll(widgets);
    }

    @Override
    public void method_21925(int mouseX, int mouseY, float delta) {
        this.method_21946();
        this.buttonListWidget.method_21897(mouseX, mouseY, delta);
        super.method_21925(mouseX, mouseY, delta);
        method_21881(this.field_22540, new TranslatableText("speedrunigt.title.options").asFormattedString(), this.field_22535 / 2, 10, 16777215);
        field_22540.method_956("v"+ SpeedRunIGT.MOD_VERSION, 4, 4, 16777215);

        ArrayList<String> tooltip = getToolTip(mouseX, mouseY);
        if (!tooltip.isEmpty()) this.method_21932(tooltip, 0, field_22536);
    }

    public ArrayList<String> getToolTip(int mouseX, int mouseY) {
        ArrayList<String> tooltipList = new ArrayList<>();


        int e = buttonListWidget.method_21910(mouseX, mouseY);
        if (e > -1) {
            ButtonWidget element = buttonListWidget.method_6697(e).getButtonWidget();
            if (tooltips.containsKey(element)) {
                String text = tooltips.get(element).get();
                tooltipList.addAll(Arrays.asList(text.split("\n")));
                return tooltipList;
            }
        }


        if (SpeedRunIGTUpdateChecker.UPDATE_STATUS == SpeedRunIGTUpdateChecker.UpdateStatus.OUTDATED) {
            tooltipList.add(new TranslatableText("speedrunigt.message.update_found").asFormattedString());
        }
        return tooltipList;
    }


    public void selectCategory(String key) {
        if (categorySelectButtons.containsKey(key) && categorySubButtons.containsKey(key)) {
            if (categorySelectButtons.containsKey(currentSelectCategory)) categorySelectButtons.get(currentSelectCategory).field_22511 = true;
            currentSelectCategory = key;

            categorySelectButtons.get(key).field_22511 = false;
            buttonListWidget.replaceButtons(categorySubButtons.get(key));
            buttonListWidget.method_21917(0);
        }
    }

    class ButtonScrollListWidget extends EntryListWidget {

        public ButtonScrollListWidget() {
            super(SpeedRunOptionScreen.this.field_22534, SpeedRunOptionScreen.this.field_22535 - 140, SpeedRunOptionScreen.this.field_22536, 28, SpeedRunOptionScreen.this.field_22536 - 54, 24);
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
        public int method_21909() {
            return 150;
        }

        private final ArrayList<ButtonScrollListEntry> entries = new ArrayList<>();

        @Override
        public ButtonScrollListEntry method_6697(int i) {
            return entries.get(i);
        }

        @Override
        protected int method_21905() {
            return entries.size();
        }

        @Override
        public void method_21897(int mouseX, int mouseY, float delta) {
            super.method_21897(mouseX, mouseY, delta);

            //Render bg on empty space
            if (SpeedRunOptionScreen.this.field_22534 == null) return;
            int emptyWidth = this.field_22514;
            GL11.glDisable(2896);
            GL11.glDisable(2912);
            Tessellator var2 = Tessellator.INSTANCE;
            SpeedRunOptionScreen.this.field_22534.getTextureManager().bindTexture(field_22503);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            float var3 = 32.0F;
            var2.method_1405();
            var2.method_1413(4210752);
            var2.method_1399(emptyWidth, SpeedRunOptionScreen.this.field_22536, 0.0D, emptyWidth / var3, ((float)SpeedRunOptionScreen.this.field_22536 / var3));
            var2.method_1399(SpeedRunOptionScreen.this.field_22535, SpeedRunOptionScreen.this.field_22536, 0.0D, ((float)SpeedRunOptionScreen.this.field_22535 / var3), (float)SpeedRunOptionScreen.this.field_22536 / var3);
            var2.method_1399(SpeedRunOptionScreen.this.field_22535, 0.0D, 0.0D, ((float)SpeedRunOptionScreen.this.field_22535 / var3), 0);
            var2.method_1399(emptyWidth, 0.0D, 0.0D, emptyWidth / var3, 0);
            var2.method_1396();
        }

        class ButtonScrollListEntry implements class_1803 {
            private final ButtonWidget buttonWidget;

            public ButtonScrollListEntry(ButtonWidget buttonWidget) {
                this.buttonWidget = buttonWidget;
                this.buttonWidget.x = (ButtonScrollListWidget.this.field_22514 - this.buttonWidget.method_21890()) / 2;
            }

            public ButtonWidget getButtonWidget() {
                return buttonWidget;
            }

            @Override
            public void method_6700(int index, int x, int y, int rowWidth, int rowfield_22536, Tessellator tessellator, int mouseX, int mouseY, boolean hovered) {
                buttonWidget.y = y;
                buttonWidget.method_21887(SpeedRunOptionScreen.this.field_22534, mouseX, mouseY);
            }

            @Override
            public boolean method_6699(int i, int j, int k, int l, int m, int n) {
                return false;
            }

            @Override
            public void method_6701(int i, int j, int k, int l, int m, int n) {

            }
        }
    }

}
