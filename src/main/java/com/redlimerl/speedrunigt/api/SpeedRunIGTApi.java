package com.redlimerl.speedrunigt.api;


import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.running.RunCategory;
import com.redlimerl.speedrunigt.timer.running.RunSplitType;
import net.fabricmc.loader.api.ModContainer;

import java.util.ArrayList;
import java.util.Collection;

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

    default RunSplitType registerSplitType() {
        return null;
    }

    default Collection<RunSplitType> registerSplitTypes() {
        return new ArrayList<>();
    }
}
