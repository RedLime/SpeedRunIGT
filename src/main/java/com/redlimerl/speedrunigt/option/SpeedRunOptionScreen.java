package com.redlimerl.speedrunigt.option;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
/**
 * @author Void_X_Walker
 * @reason Backported to 1.8, redid almost everything because 1.8 screens and buttons work completely different
 */
public class SpeedRunOptionScreen extends Screen {

    private final Screen parent;
    private final int page;
private final String title = "Timer Options";
    public SpeedRunOptionScreen(Screen parent) {
        this(parent, 0);
    }

    public SpeedRunOptionScreen(Screen parent, int page) {
        super();
        this.page = page;
        this.parent = parent;
    }

    static HashMap<ButtonWidget, List<Text>> tooltips = new HashMap<>();
    @Override
    public void init() {
        super.init();

        int buttonCount = 0;
        for (Function<Screen, ButtonWidget> function : SpeedRunOptions.buttons.subList(page*12, Math.min(SpeedRunOptions.buttons.size(), (page + 1) * 12))) {
            ButtonWidget button = function.apply(this);
            tooltips.put(button, SpeedRunOptions.tooltips.get(function));

            button.x = width / 2 - 155 + buttonCount % 2 * 160;
            button.y = height / 6 - 12 + 24 * (buttonCount / 2);
           buttons.add(button);
            buttonCount++;
        }

        buttons.add(new ButtonWidget(6001,width / 2 - 100, height / 6 + 168, 200, 20, ScreenTexts.DONE));

        if (SpeedRunOptions.buttons.size() > 12) {
            ButtonWidget nextButton = new ButtonWidget(6002,width / 2 - 155 + 260, height / 6 + 144, 50, 20,">>>");
            buttons.add(nextButton);
            ButtonWidget prevButton = new ButtonWidget(6003,width / 2 - 155, height / 6 + 144, 50, 20, "<<<");
            buttons.add(prevButton);
            if ((SpeedRunOptions.buttons.size() - 1) / 12 == page) {
                nextButton.active = false;
            }
            if (page == 0) {
                prevButton.active = false;
            }
        }
    }

    protected void buttonClicked(ButtonWidget button) {
        if(button.id==6001){
            if (client != null) client.openScreen(parent);
        }
        else if(button.id==6002){
            if (client != null) client.openScreen(new SpeedRunOptionScreen(parent, page + 1));
        }
        else if(button.id==6003){
            if (client != null) client.openScreen(new SpeedRunOptionScreen(parent, page - 1));
        }
        else if(button.id==900){
            MinecraftClient.getInstance().openScreen(new TimerCustomizeScreen(this));
        }
        else if(button.id==901){
            MinecraftClient.getInstance().openScreen(new SpeedRunCategoryScreen(this));
        }
        else if(button.id==902) {
            SpeedRunIGT.TIMER_DRAWER.setToggle(! SpeedRunIGT.TIMER_DRAWER.isToggle());
            SpeedRunOptions.setOption(SpeedRunOptions.TOGGLE_TIMER,  SpeedRunIGT.TIMER_DRAWER.isToggle());
            button.message=(SpeedRunIGT.translate("speedrunigt.option.timer_position.toggle_timer","Toggle Timer").getString() + " : " + ( SpeedRunIGT.TIMER_DRAWER.isToggle() ? ScreenTexts.ON : ScreenTexts.OFF));
        }
        else if(button.id==903){
                SpeedRunOptions.setOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS, !SpeedRunOptions.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS));
                button.message=(SpeedRunIGT.translate("speedrunigt.option.hide_timer_in_options","Hide Timer in Options").getString() + " : " + (SpeedRunOptions.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS) ? ScreenTexts.ON : ScreenTexts.OFF));
        }
        else if(button.id==904){
                SpeedRunOptions.setOption(SpeedRunOptions.WAITING_FIRST_INPUT, !SpeedRunOptions.getOption(SpeedRunOptions.WAITING_FIRST_INPUT));
                button.message=(SpeedRunIGT.translate("speedrunigt.option.waiting_first_input","Start at First Input").getString() + " : " + (SpeedRunOptions.getOption(SpeedRunOptions.WAITING_FIRST_INPUT) ? ScreenTexts.ON : ScreenTexts.OFF));
        }


    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        this.drawCenteredString(this.textRenderer, this.title, this.width / 2, 15, 16777215);
        super.render(mouseX, mouseY, delta);

    }
}
