package com.redlimerl.speedrunigt.impl;

import com.google.common.collect.Maps;
import com.redlimerl.speedrunigt.api.CategoryConditionRegisterHelper;
import com.redlimerl.speedrunigt.api.SpeedRunIGTApi;
import com.redlimerl.speedrunigt.timer.category.condition.AdvancementCategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.DummyCategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.ObtainItemCategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.StatCategoryCondition;

import java.util.HashMap;
import java.util.Map;

public class ConditionsRegistryImpl implements SpeedRunIGTApi {

    @Override
    public Map<String, CategoryConditionRegisterHelper> registerConditions() {
        HashMap<String, CategoryConditionRegisterHelper> hashMap = Maps.newHashMap();
        hashMap.put("achieve_advancement", AdvancementCategoryCondition::new);
        hashMap.put("obtain_item", ObtainItemCategoryCondition::new);
        hashMap.put("player_stat", StatCategoryCondition::new);
        hashMap.put("dummy", DummyCategoryCondition::new);
        return hashMap;
    }

}

