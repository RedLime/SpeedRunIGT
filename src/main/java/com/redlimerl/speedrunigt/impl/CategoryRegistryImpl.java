package com.redlimerl.speedrunigt.impl;

import com.redlimerl.speedrunigt.api.SpeedRunIGTApi;
import com.redlimerl.speedrunigt.timer.PracticeTimerManager;
import com.redlimerl.speedrunigt.timer.category.RunCategory;

import java.util.ArrayList;
import java.util.Collection;

import static com.redlimerl.speedrunigt.timer.category.RunCategories.*;

public class CategoryRegistryImpl implements SpeedRunIGTApi {
    @Override
    public Collection<RunCategory> registerCategories() {
        ArrayList<RunCategory> list = new ArrayList<>();
        list.add(ANY);list.add(CUSTOM);
        list.add(PracticeTimerManager.PRACTICE_CATEGORY);
        list.add(ALL_ADVANCEMENTS);list.add(HALF);list.add(POGLOOT_QUATER);
        list.add(ALL_PORTALS);
        list.add(KILL_ALL_BOSSES);list.add(KILL_WITHER);list.add(KILL_ELDER_GUARDIAN);
        list.add(HOW_DID_WE_GET_HERE);
        list.add(ENTER_NETHER);list.add(ENTER_END);
        list.add(MINE_A_CHUNK);
        list.add(HIGH);
        list.add(ALL_SWORDS);list.add(ALL_MINERALS);list.add(FULL_IA_15_LVL);list.add(FULL_INV);list.add(STACK_OF_LIME_WOOL);
        return list;
    }
}
