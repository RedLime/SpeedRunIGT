package com.redlimerl.speedrunigt.timer.category.condition;

import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import java.util.Objects;
import net.minecraft.advancement.Advancement;

public class AdvancementCategoryCondition extends CategoryCondition.Condition<Advancement> {

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
    public boolean checkConditionComplete(Advancement obj) {
        return Objects.equals(obj.getId().toString(), advancement);
    }
}
