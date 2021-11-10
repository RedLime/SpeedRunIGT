package com.redlimerl.speedrunigt;

import com.redlimerl.speedrunigt.option.SpeedRunCategoryScreen;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.TimerPosition;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.Locale;

public class SpeedRunIGT implements ClientModInitializer {

    public static final String MOD_ID = "speedrunigt";
    public static final Identifier BUTTON_ICON_TEXTURE = new Identifier(MOD_ID, "textures/gui/buttons.png");

    @Override
    public void onInitializeClient() {
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.timer_position").append(": ").append(
                                new TranslatableText("speedrunigt.option.timer_position."+ SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POS).name().toLowerCase(Locale.ROOT))
                        ), (ButtonWidget button) -> {
                    SpeedRunOptions.setOption(SpeedRunOptions.TIMER_POS, getTimePosNext(SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POS)));
                    button.setMessage(new TranslatableText("speedrunigt.option.timer_position").append(": ").append(
                            new TranslatableText("speedrunigt.option.timer_position."+ SpeedRunOptions.getOption(SpeedRunOptions.TIMER_POS).name().toLowerCase(Locale.ROOT))
                    ));
                }
                )
        );
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.timer_category"), (ButtonWidget button) -> MinecraftClient.getInstance().openScreen(new SpeedRunCategoryScreen(screen)))
        );
        SpeedRunOptions.init();
    }

    public static void debug(Object obj) {
        System.out.println(obj);
    }

    private static TimerPosition getTimePosNext(TimerPosition tp) {
        TimerPosition[] v = TimerPosition.values();
        return v[(tp.ordinal() + 1) % v.length];
    }
}
