package com.redlimerl.speedrunigt.timer.category.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.api.CategoryConditionRegisterHelper;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import org.spongepowered.include.com.google.common.collect.Lists;
import org.spongepowered.include.com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryCondition implements Serializable {

    private static class Conditions implements Serializable {
        private final ArrayList<Condition<?>> conditions = new ArrayList<>();
    }

    @SuppressWarnings("FieldCanBeLocal")
    public static class Condition<T> implements Serializable {
        private final String name;
        boolean isCompleted = false;

        public Condition(JsonObject jsonObject) throws InvalidCategoryException {
            if (!jsonObject.has("name"))
                throw new InvalidCategoryException(InvalidCategoryException.Reason.INVALID_JSON_DATA, "condition \"name\" is undefined.");
            this.name = jsonObject.get("name").getAsString();
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public void setCompleted(boolean completed) {
            isCompleted = completed;
        }

        public String getName() {
            return name;
        }

        public boolean checkConditionComplete(T obj) {
            return false;
        }
    }

    private static final HashMap<String, CategoryConditionRegisterHelper> CONDITION_HASHMAP = Maps.newHashMap();
    private static Condition<?> getConditionType(JsonObject jsonObject) throws InvalidCategoryException {
        if (!jsonObject.has("type"))
            throw new InvalidCategoryException(InvalidCategoryException.Reason.INVALID_JSON_DATA, "condition \"type\" is undefined");

        final String type = jsonObject.get("type").getAsString();
        if (CONDITION_HASHMAP.containsKey(type)) {
            return CONDITION_HASHMAP.get(type).create(jsonObject);
        }

        throw new InvalidCategoryException(InvalidCategoryException.Reason.UNKNOWN_EVENT_TYPE, "");
    }
    public static void registerCondition(Map<String, CategoryConditionRegisterHelper> register) {
        if (SpeedRunIGT.isInitialized()) return;
        for (Map.Entry<String, CategoryConditionRegisterHelper> helper : register.entrySet()) {
            if (CONDITION_HASHMAP.containsKey(helper.getKey())) {
                throw new IllegalArgumentException("ID \"" + helper.getKey() + "\" is an already registered Condition ID.");
            }
        }

        CONDITION_HASHMAP.putAll(register);
    }

    private final ArrayList<Conditions> availableConditions = new ArrayList<>();

    public CategoryCondition(JsonArray jsonArray) throws InvalidCategoryException {
        ArrayList<Conditions> conditions = new ArrayList<>();
        try {
            for (JsonElement jsonElement : jsonArray) {
                JsonArray jsonConditions = jsonElement.getAsJsonArray();
                Conditions andConditions = new Conditions();

                for (JsonElement jsonCondition : jsonConditions) {
                    andConditions.conditions.add(getConditionType(jsonCondition.getAsJsonObject()));
                }

                conditions.add(andConditions);
            }
        } catch (InvalidCategoryException throwable) {
            throw throwable;
        } catch (Throwable throwable) {
            throw new InvalidCategoryException(InvalidCategoryException.Reason.INVALID_JSON_DATA, "");
        }

        this.availableConditions.addAll(conditions);
    }


    public boolean isDone() {
        for (Conditions conditionList : availableConditions) {
            int done = 0;
            for (Condition<?> condition : conditionList.conditions) {
                if (condition.isCompleted) done++;
            }
            if (done == conditionList.conditions.size()) return true;
        }
        return false;
    }

    public List<Condition<?>> getConditionList() {
        ArrayList<Condition<?>> list = Lists.newArrayList();
        for (Conditions availableCondition : availableConditions) list.addAll(availableCondition.conditions);
        return list;
    }
}
