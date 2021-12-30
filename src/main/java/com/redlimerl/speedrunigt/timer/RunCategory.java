package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.minecraft.text.Text;

import java.util.Locale;
/**
 * @author Void_X_Walker
 * @reason Backported to 1.8, redid almost everything because 1.8 screens and buttons work completely different
 */
public enum RunCategory {
    ANY("mc#Any_Glitchless","Any%"),
    CUSTOM("mc#","Custom (for Co-op, etc)"),
    HIGH("mcce#High","High%"),
    KILL_ALL_BOSSES("mcce#Kill_Bosses","Kill All Bosses"),
    KILL_WITHER("mcce#Kill_Bosses","Kill Wither"),
    ALL_ACHIEVEMENTS("mc#All_Achievements","All Achievements"),
    HALF("mcce#Half","Half%"),
    ENTER_NETHER("mcce#Enter_Nether","Enter Nether"),
    ENTER_END("mcce#Etner_Edn","Ender End"),
    ALL_SWORDS("mcce#All_Swords","All Swords"),
    ALL_MINERALS("mcce#All_Minerals","All Minerals"),
    FULL_IA_15_LVL("mcce#Full_Iron_Armor_and_15_Levels","Full IA, 15 LVL"),
    FULL_INV("mcce#Full_Inventory","Full Inventory");

    String code;
    String alternative;
    RunCategory(String code, String alternative) {
        this.code = code;
        this.alternative=alternative;
    }

    public String getCode() {
        return code;
    }

    public Text getText() {
        return SpeedRunIGT.translate("speedrunigt.option.timer_category." + this.name().toLowerCase(Locale.ROOT),this.alternative);
    }
}
