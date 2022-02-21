package com.redlimerl.speedrunigt.impl;

import com.redlimerl.speedrunigt.api.SpeedRunIGTApi;
import com.redlimerl.speedrunigt.timer.running.RunSplitType;
import com.redlimerl.speedrunigt.timer.running.RunSplitTypes;

import java.util.ArrayList;
import java.util.Collection;

public class SplitTypeRegistryImpl implements SpeedRunIGTApi {
    @Override
    public Collection<RunSplitType> registerSplitTypes() {
        ArrayList<RunSplitType> list = new ArrayList<>();

        list.add(RunSplitTypes.COMPLETE); list.add(RunSplitTypes.ERROR_SPLIT);

        list.add(RunSplitTypes.ENTER_NETHER); list.add(RunSplitTypes.ENTER_FORTRESS); list.add(RunSplitTypes.ENTER_BASTION);
        list.add(RunSplitTypes.ENTER_END); list.add(RunSplitTypes.ENTER_STRONG_HOLD);

        return list;
    }
}
