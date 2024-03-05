package com.redlimerl.speedrunigt.timer.category.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;

public class StatCategoryCondition extends CategoryCondition.Condition<JsonObject> {

    private Integer stat;
    private final int goal;

    public StatCategoryCondition(JsonObject jsonObject) throws InvalidCategoryException {
        super(jsonObject);

        try {
            String statTranslation = jsonObject.get("stat").getAsString();
            for (Object object : Stats.ALL) {
                Stat stat_ = (Stat) object;
                if (stat_.getStringId().equals(statTranslation)) {
                    stat = stat_.id;
                    break;
                }
            }
            if (stat == null) {
                throw new IllegalStateException("bad stat");
            }
            this.goal = jsonObject.get("goal").getAsInt();
        } catch (Exception e) {
            throw new InvalidCategoryException(InvalidCategoryException.Reason.INVALID_JSON_DATA, "Failed to read condition \"stat\" or \"goal\" in \"" + this.getName() + "\"");
        }
    }

    @Override
    public boolean checkConditionComplete(JsonObject statObject) {
        // TODO: fixey-fixey (print json out)
//        try {
//            JsonObject jsonElement = statObject.get(stat.toString()).getAsJsonObject();
//            if (jsonElement.isJsonObject()) {
//                int count = jsonElement.getAsNumber().intValue();
//                if (count >= goal) return true;
//            } else {
//                int count = statObject.get(stat).getAsInt();
//                if (count >= goal) return true;
//            }
//        } catch (Exception e) {
//            return false;
//        }
        return false;
    }
}
