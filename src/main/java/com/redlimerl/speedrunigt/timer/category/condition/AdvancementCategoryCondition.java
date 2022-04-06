package com.redlimerl.speedrunigt.timer.category.condition;

import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import net.minecraft.class_3326;

import java.util.Objects;

public class AdvancementCategoryCondition extends CategoryCondition.Condition<class_3326> {

    private final String advancement;

    public AdvancementCategoryCondition(JsonObject jsonObject) throws InvalidCategoryException {
        super(jsonObject);

        try {
            this.advancement = jsonObject.get("advancement").getAsString();
        } catch (Exception e) {
            throw new InvalidCategoryException(InvalidCategoryException.Reason.INVALID_JSON_DATA, "Failed to read condition \"advancement\"");
        }
    }

    @Override
    public boolean checkConditionComplete(class_3326 obj) {
        return Objects.equals(obj.method_14801().toString(), advancement);
    }
}
