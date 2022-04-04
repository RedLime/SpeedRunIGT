package com.redlimerl.speedrunigt.api;

import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;

public interface CategoryConditionRegisterHelper {
    CategoryCondition.Condition<?> create(JsonObject jsonObject) throws InvalidCategoryException;
}
