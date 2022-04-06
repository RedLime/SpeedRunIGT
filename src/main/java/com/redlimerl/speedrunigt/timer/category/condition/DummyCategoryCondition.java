package com.redlimerl.speedrunigt.timer.category.condition;

import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;

public class DummyCategoryCondition extends CategoryCondition.Condition<Object> {

    public DummyCategoryCondition(JsonObject jsonObject) throws InvalidCategoryException {
        super(jsonObject);
    }

    @Override
    public boolean checkConditionComplete(Object obj) {
        return false;
    }
}
