package com.redlimerl.speedrunigt.timer.category.condition;

import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;

public class StatCategoryCondition extends CategoryCondition.Condition<JsonObject> {

    private final String stat;
    private final int goal;

    public StatCategoryCondition(JsonObject jsonObject) throws InvalidCategoryException {
        super(jsonObject);

        try {
            this.stat = jsonObject.get("stat").getAsString();
            this.goal = jsonObject.get("goal").getAsInt();
        } catch (Exception e) {
            throw new InvalidCategoryException(InvalidCategoryException.Reason.INVALID_JSON_DATA, "Failed to read condition \"stat\" or \"goal\" in \"" + this.getName() + "\"");
        }
    }

    @Override
    public boolean checkConditionComplete(JsonObject statObject) {
        try {
            JsonObject jsonElement = statObject.get(stat).getAsJsonObject();
            if (jsonElement.isJsonObject()) {
                int count = jsonElement.get("value").getAsInt();
                if (count >= goal) return true;
            } else {
                int count = statObject.get(stat).getAsInt();
                if (count >= goal) return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
