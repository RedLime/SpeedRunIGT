package com.redlimerl.speedrunigt.timer.running;

import com.redlimerl.speedrunigt.SpeedRunIGT;

import java.util.HashMap;
import java.util.Map;

public class RunSplitType {

    private static final HashMap<String, RunSplitType> SPLIT_TYPES = new HashMap<>();
    public static Map<String, RunSplitType> getSplitTypes() {
        return SPLIT_TYPES;
    }
    public static RunSplitType getSplitType(String id) {
        if (!SPLIT_TYPES.containsKey(id)) {
            SpeedRunIGT.error("Not found split with ID \"" + id + "\"!");
            return RunSplitTypes.ERROR_SPLIT;
        }
        return SPLIT_TYPES.get(id);
    }

    public static void registrySplitType(RunSplitType split) {
        if (SpeedRunIGT.isInitialized()) return;
        for (String id : SPLIT_TYPES.keySet()) {
            if (id.equalsIgnoreCase(split.getID())) {
                throw new IllegalArgumentException("ID \"" + id + "\" is an already registered RunSplitType ID.");
            }
        }

        SPLIT_TYPES.put(split.getID(), split);
    }


    private final String id;
    private final String translateKey;

    public RunSplitType(String id, String translateKey) {
        this.id = id;
        this.translateKey = translateKey;
    }

    public String getID() {
        return id;
    }

    public String getTranslateKey() {
        return translateKey;
    }
}
