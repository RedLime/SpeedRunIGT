package com.redlimerl.speedrunigt.timer.category;

import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.running.RunType;

import java.util.function.Function;

public class RunCategories {
    public static RunCategory ERROR_CATEGORY = new RunCategory("unknown","mc");

    public static Function<InGameTimer, Boolean> anyPercentRetime = timer ->
            !SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE) && !timer.isCoop() && timer.getRunType() == RunType.RANDOM_SEED && !timer.isRTAMode() &&
                    (SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME) || timer.getInGameTime(false) < 1000 * 60 * 13);
    public static RunCategory ANY = RunCategoryBuilder.create("ANY", "mc", "speedrunigt.option.timer_category.any")
            .setRetimeFunction(anyPercentRetime).build();
    public static RunCategory CUSTOM = new RunCategory("CUSTOM","mc#");
    public static RunCategory HIGH = new RunCategory("HIGH","mcce#High");
    public static RunCategory KILL_WARDEN = new RunCategory("KILL_WARDEN","mcce#Kill_Bosses");
    public static RunCategory KILL_ALL_BOSSES = new RunCategory("KILL_ALL_BOSSES","mcce#Kill_Bosses");
    public static RunCategory KILL_WITHER = new RunCategory("KILL_WITHER","mcce#Kill_Bosses");
    public static RunCategory KILL_ELDER_GUARDIAN = new RunCategory("KILL_ELDER_GUARDIAN","mcce#Kill_Bosses");
    public static RunCategory ALL_ADVANCEMENTS = RunCategoryBuilder.create("ALL_ADVANCEMENTS","mc#All_Advancements", "speedrunigt.option.timer_category.all_advancements")
            .setCanSegment(true).build();
    public static RunCategory HALF = new RunCategory("HALF","mcce#Half");
    public static RunCategory POGLOOT_QUATER = new RunCategory("POGLOOT_QUATER","pogloot_ce#Quater");
    public static RunCategory HOW_DID_WE_GET_HERE = new RunCategory("HOW_DID_WE_GET_HERE","mcce#How_Did_We_Get_Here", "advancements.nether.all_effects.title");
    public static RunCategory HERO_OF_VILLAGE = new RunCategory("HERO_OF_VILLAGE","mcce#Hero_of_the_Village", "advancements.adventure.hero_of_the_village.title");
    public static RunCategory ARBALISTIC = new RunCategory("ARBALISTIC","mcce#Arbalistic", "advancements.adventure.arbalistic.title");
    public static RunCategory COVER_ME_IN_DEBRIS = new RunCategory("COVER_ME_IN_DEBRIS","mcce#Cover_Me_in_Debris", "advancements.nether.netherite_armor.title");
    public static RunCategory ENTER_NETHER = new RunCategory("ENTER_NETHER","mcce#Enter_Nether");
    public static RunCategory ENTER_END = new RunCategory("ENTER_END","mcce#Etner_Edn");
    public static RunCategory ALL_SWORDS = new RunCategory("ALL_SWORDS","mcce#All_Swords");
    public static RunCategory ALL_MINERALS = new RunCategory("ALL_MINERALS","mcce#All_Minerals");
    public static RunCategory FULL_IA_15_LVL = new RunCategory("FULL_IA_15_LVL","mcce#Full_Iron_Armor_and_15_Levels");
    public static RunCategory ALL_WORKSTATIONS = new RunCategory("ALL_WORKSTATIONS","mcce#All_Workstations");
    public static RunCategory FULL_INV = new RunCategory("FULL_INV","mcce#Full_Inventory");
    public static RunCategory STACK_OF_LIME_WOOL = new RunCategory("STACK_OF_LIME_WOOL","mcce#Stack_of_Lime_Wool");
    public static RunCategory ALL_PORTALS = RunCategoryBuilder.create("ALL_PORTALS","mcce#All_Portals", "speedrunigt.option.timer_category.all_portals")
            .setCanSegment(true).build();
    public static AllBlocksRunCategory ALL_BLOCKS = new AllBlocksRunCategory();
    public static RunCategory MINE_A_CHUNK = new RunCategory("MINE_A_CHUNK","mcce#Mine_a_Chunk");

    public static void checkAllBossesCompleted() {
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getCategory() == KILL_ALL_BOSSES) {
            if (timer.getMoreData(0) + timer.getMoreData(1) + timer.getMoreData(2) + timer.getMoreData(3) == 4) {
                InGameTimer.complete();
            }
        }
    }
}
