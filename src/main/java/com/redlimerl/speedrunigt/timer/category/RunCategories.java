package com.redlimerl.speedrunigt.timer.category;

import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.therun.TheRunCategory;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.running.RunType;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.LinkedHashMap;
import java.util.Map;

public class RunCategories {

    public static RunCategory ERROR_CATEGORY = new RunCategory("unknown","mc");

    public static RunCategory ANY = RunCategoryBuilder.create("ANY", "mc", "speedrunigt.option.timer_category.any")
            .setTheRunCategory(
                    new TheRunCategory.Builder()
                            .setGameName("Minecraft: Java Edition")
                            .setCategoryNameFunction(timer -> timer.getRunType() == RunType.RANDOM_SEED ? "Any% RSG" : "Any% SSG")
                            .setSplitNameMap(timer -> {
                                if (timer.getRunType() == RunType.SET_SEED) {
                                    return asHashMap(
                                            new MutablePair<>("enter_end", "Enter The End")
                                    );
                                } else if (timer.getRunType() == RunType.RANDOM_SEED) {
                                    return asHashMap(
                                            new MutablePair<>("enter_nether", "Enter Nether"),
                                            new MutablePair<>("enter_fortress", "Found Fortress"),
                                            new MutablePair<>("enter_stronghold", "Eye Spy"),
                                            new MutablePair<>("enter_end", "Enter The End")
                                    );
                                } else {
                                    return null;
                                }
                            })
                            .setCompletedSplitName("Defeat Ender Dragon")
                            .build()
            )
            .setRetimeFunction(timer ->
                    !SpeedRunOption.getOption(SpeedRunOptions.TIMER_LEGACY_IGT_MODE) && !timer.isCoop() && timer.getRunType() == RunType.RANDOM_SEED && !timer.isRTAMode() &&
                            (SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_USE_AUTO_RETIME) || timer.getInGameTime(false) < 1000 * 60 * 30)
            ).build();
    public static RunCategory CUSTOM = new RunCategory("CUSTOM","mc#");
    public static RunCategory HIGH = new RunCategory("HIGH","mcce#High");
    public static RunCategory KILL_ALL_BOSSES = new RunCategory("KILL_ALL_BOSSES","mcce#Kill_Bosses");
    public static RunCategory KILL_WITHER = new RunCategory("KILL_WITHER","mcce#Kill_Bosses");
    public static RunCategory KILL_ELDER_GUARDIAN = new RunCategory("KILL_ELDER_GUARDIAN","mcce#Kill_Bosses");
    public static RunCategory ALL_ADVANCEMENTS = RunCategoryBuilder.create("ALL_ADVANCEMENTS","mc#All_Advancements", "speedrunigt.option.timer_category.all_advancements")
            .setCanSegment(true).build();
    public static RunCategory HALF = new RunCategory("HALF","mcce#Half");
    public static RunCategory POGLOOT_QUATER = new RunCategory("POGLOOT_QUATER","pogloot_ce#Quater");
    public static RunCategory HOW_DID_WE_GET_HERE = new RunCategory("HOW_DID_WE_GET_HERE","mcce#How_Did_We_Get_Here", "advancements.nether.all_effects.title");
    public static RunCategory ENTER_NETHER = new RunCategory("ENTER_NETHER","mcce#Enter_Nether");
    public static RunCategory ENTER_END = new RunCategory("ENTER_END","mcce#Etner_Edn");
    public static RunCategory ALL_SWORDS = new RunCategory("ALL_SWORDS","mcce#All_Swords");
    public static RunCategory ALL_MINERALS = new RunCategory("ALL_MINERALS","mcce#All_Minerals");
    public static RunCategory FULL_IA_15_LVL = new RunCategory("FULL_IA_15_LVL","mcce#Full_Iron_Armor_and_15_Levels");
    public static RunCategory FULL_INV = new RunCategory("FULL_INV","mcce#Full_Inventory");
    public static RunCategory STACK_OF_LIME_WOOL = new RunCategory("STACK_OF_LIME_WOOL","mcce#Stack_of_Lime_Wool");
    public static RunCategory ALL_PORTALS = RunCategoryBuilder.create("ALL_PORTALS","mcce#All_Portals", "speedrunigt.option.timer_category.all_portals")
            .setCanSegment(true).build();
    public static RunCategory MINE_A_CHUNK = new RunCategory("MINE_A_CHUNK","mcce#Mine_a_Chunk");

    public static void checkAllBossesCompleted() {
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getCategory() == KILL_ALL_BOSSES) {
            if (timer.getMoreData(0) + timer.getMoreData(1) + timer.getMoreData(2) == 3) {
                InGameTimer.complete();
            }
        }
    }

    @SafeVarargs
    public static LinkedHashMap<String, String> asHashMap(Map.Entry<String, String>... entries) {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            linkedHashMap.put(entry.getKey(), entry.getValue());
        }
        return linkedHashMap;
    }
}
