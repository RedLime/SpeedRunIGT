package com.redlimerl.speedrunigt.timer;

import net.minecraft.client.resource.language.I18n;

import java.util.Locale;

public enum RunCategory {
    ANY("mc#Any_Glitchless"),
    CUSTOM("mc#"),
    HIGH("mcce#High"),
    KILL_ALL_BOSSES("mcce#Kill_Bosses"),
    KILL_WITHER("mcce#Kill_Bosses"),
    KILL_ELDER_GUARDIAN("mcce#Kill_Bosses"),
    ALL_ADVANCEMENTS("mc#All_Advancements"),
    HALF("mcce#Half"),
    HOW_DID_WE_GET_HERE("mcce#How_Did_We_Get_Here"),
    HERO_OF_VILLAGE("mcce#Hero_of_the_Village"),
    ARBALISTIC("mcce#Arbalistic"),
    ENTER_NETHER("mcce#Enter_Nether"),
    ENTER_END("mcce#Etner_Edn"),
    ALL_SWORDS("mcce#All_Swords"),
    ALL_MINERALS("mcce#All_Minerals"),
    FULL_IA_15_LVL("mcce#Full_Iron_Armor_and_15_Levels"),
    ALL_WORKSTATIONS("mcce#All_Workstations"),
    FULL_INV("mcce#Full_Inventory"),
    STACK_OF_LIME_WOOL("mcce#Stack_of_Lime_Wool");

    String code;
    RunCategory(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return I18n.translate("speedrunigt.option.timer_category." + this.name().toLowerCase(Locale.ROOT));
    }
}
