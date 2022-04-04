package com.redlimerl.speedrunigt.api;


import com.google.common.collect.Maps;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import net.fabricmc.loader.api.ModContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public interface SpeedRunIGTApi {
    static ModContainer[] getProviders() {
        return SpeedRunIGT.API_PROVIDERS.toArray(new ModContainer[0]);
    }


    default OptionButtonFactory createOptionButton() {
        return null;
    }

    default Collection<OptionButtonFactory> createOptionButtons() {
        return new ArrayList<>();
    }

    default RunCategory registerCategory() {
        return null;
    }

    default Collection<RunCategory> registerCategories() {
        return new ArrayList<>();
    }

    default Map<String, CategoryConditionRegisterHelper> registerConditions() {
        return Maps.newHashMap();
    }
}
