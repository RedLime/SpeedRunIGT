package com.redlimerl.speedrunigt.impl;

import com.redlimerl.speedrunigt.api.SpeedRunIGTApi;
import com.redlimerl.speedrunigt.timer.category.RunCategory;

import java.util.ArrayList;
import java.util.Collection;

import static com.redlimerl.speedrunigt.timer.category.RunCategories.*;

public class CategoryRegistryImpl implements SpeedRunIGTApi {
    @Override
    public Collection<RunCategory> registerCategories() {
        ArrayList<RunCategory> list = new ArrayList<>();
        list.add(ANY);list.add(CUSTOM);list.add(HIGH);list.add(KILL_ALL_BOSSES);list.add(KILL_WITHER);list.add(KILL_ELDER_GUARDIAN);list.add(ALL_ACHIEVEMENTS);
        list.add(HALF);list.add(POGLOOT_QUATER);list.add(HOW_DID_WE_GET_HERE);list.add(HERO_OF_VILLAGE);list.add(ARBALISTIC);list.add(ENTER_NETHER);list.add(ENTER_END);
        list.add(ALL_SWORDS);list.add(ALL_MINERALS);list.add(FULL_IA_15_LVL);list.add(ALL_WORKSTATIONS);list.add(FULL_INV);list.add(STACK_OF_LIME_WOOL);
        return list;
    }
}
