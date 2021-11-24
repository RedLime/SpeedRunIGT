package com.redlimerl.speedrunigt;

import com.redlimerl.speedrunigt.option.SpeedRunCategoryScreen;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.option.TimerCustomizeScreen;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class SpeedRunIGT implements ClientModInitializer {

    public static final String MOD_ID = "speedrunigt";
    public static final Identifier BUTTON_ICON_TEXTURE = new Identifier(MOD_ID, "textures/gui/buttons.png");
    public static final TimerDrawer TIMER_DRAWER = new TimerDrawer(InGameTimer.INSTANCE);

    @Override
    public void onInitializeClient() {
        SpeedRunOptions.addOptionButton(screen ->
                new ButtonWidget(0, 0, 150, 20,
                        new TranslatableText("speedrunigt.option.timer_position"), (ButtonWidget button) -> MinecraftClient.getInstance().openScreen(new TimerCustomizeScreen(screen)))
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
}
