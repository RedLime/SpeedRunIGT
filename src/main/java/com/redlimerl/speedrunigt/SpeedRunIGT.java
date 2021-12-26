package com.redlimerl.speedrunigt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import java.nio.file.Path;

/**
 * @author Void_X_Walker
 * @reason Backported to 1.8
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class SpeedRunIGT implements ClientModInitializer {

    public static final String MOD_ID = "speedrunigt";
    public static final TimerDrawer TIMER_DRAWER = new TimerDrawer(true);

    public static String DEBUG_DATA = "";

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path MAIN_PATH = FabricLoader.getInstance().getGameDir().resolve(MOD_ID);
    public static final Path WORLDS_PATH = FabricLoader.getInstance().getGameDir().resolve("saves");
    public static final Path TIMER_PATH = MAIN_PATH.resolve("worlds");
    static {
        MAIN_PATH.toFile().mkdirs();
        TIMER_PATH.toFile().mkdirs();
    }


    public void onInitializeClient() {
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(900,0, 0, 150, 20,
                        SpeedRunIGT.translate("speedrunigt.option.timer_position","Timer Display Options").getString())
        );
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(901,0, 0, 150, 20,
                        SpeedRunIGT.translate("speedrunigt.option.timer_category","Timer Category").getString())
        );
        SpeedRunOptions.addOptionButton(screen -> new ButtonWidget(902,0, 0, 150, 20, SpeedRunIGT.translate("speedrunigt.option.timer_position.toggle_timer","Toggle Timer").getString() + " : " + (TIMER_DRAWER.isToggle() ? ScreenTexts.ON : ScreenTexts.OFF)));
        SpeedRunOptions.addOptionButton(screen -> new ButtonWidget(903,0, 0, 150, 20, SpeedRunIGT.translate("speedrunigt.option.hide_timer_in_options","Hide Timer in Options").getString() + " : " + (SpeedRunOptions.getOption(SpeedRunOptions.HIDE_TIMER_IN_OPTIONS) ? ScreenTexts.ON : ScreenTexts.OFF)));
        SpeedRunOptions.addOptionButton(screen ->
                        new ButtonWidget(904,0, 0, 150, 20,
                                SpeedRunIGT.translate("speedrunigt.option.waiting_first_input","Start at First Input").getString() + " : " + (SpeedRunOptions.getOption(SpeedRunOptions.WAITING_FIRST_INPUT) ? ScreenTexts.ON : ScreenTexts.OFF))
                , SpeedRunIGT.translate("speedrunigt.option.waiting_first_input.description","Change the timer's starting point from\nthe world load to the player's first input\n(movement, inventory, etc.).\nThis option is for categories that start on first input."));
        SpeedRunOptions.init();


    }
    public static Text translate(String key,String alternative){
        TranslatableText text=new TranslatableText(key);
        if(text.getString().equals(key)){
            return new LiteralText(alternative);
        }
        return text;
    }

}
