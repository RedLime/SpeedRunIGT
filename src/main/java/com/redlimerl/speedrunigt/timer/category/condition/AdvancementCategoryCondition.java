package com.redlimerl.speedrunigt.timer.category.condition;

import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import net.minecraft.advancements.AdvancementHolder;

import java.util.Objects;

public class AdvancementCategoryCondition extends CategoryCondition.Condition<AdvancementHolder> {

    private final String advancement;

    public AdvancementCategoryCondition(JsonObject jsonObject) throws InvalidCategoryException {
        super(jsonObject);

        try {
            this.advancement = jsonObject.get("advancement").getAsString();
        } catch (Exception e) {
            throw new InvalidCategoryException(InvalidCategoryException.Reason.INVALID_JSON_DATA, "Failed to read condition \"advancement\" in \"" + this.getName() + "\"");
        }
    }

    @Override
    public boolean checkConditionComplete(AdvancementHolder obj) {
        return Objects.equals(obj.id().toString(), advancement);
    }
}
