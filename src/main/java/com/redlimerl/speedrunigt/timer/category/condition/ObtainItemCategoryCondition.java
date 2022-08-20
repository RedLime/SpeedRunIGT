package com.redlimerl.speedrunigt.timer.category.condition;

import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Objects;

public class ObtainItemCategoryCondition extends CategoryCondition.Condition<List<ItemStack>> {

    private final String itemID;
    private final int itemDamage;
    private final int itemAmount;

    public ObtainItemCategoryCondition(JsonObject jsonObject) throws InvalidCategoryException {
        super(jsonObject);

        try {
            this.itemID = jsonObject.get("item_id").getAsString();
            this.itemAmount = jsonObject.has("item_amount") ? jsonObject.get("item_amount").getAsInt() : 1; // Optional
            this.itemDamage = jsonObject.has("item_damage") ? jsonObject.get("item_damage").getAsInt() : 0; // Optional
        } catch (Exception e) {
            throw new InvalidCategoryException(InvalidCategoryException.Reason.INVALID_JSON_DATA, "Failed to read condition \"item_id\"");
        }
    }

    @Override
    public boolean checkConditionComplete(List<ItemStack> itemStacks) {
        int amount = 0;

        for (ItemStack itemStack : itemStacks) {
            if (itemStack != null && Objects.equals(Item.getFromId(itemID), itemStack.getItem()) && itemStack.getDamage() == itemDamage)
                amount += itemStack.getCount();
        }

        return amount >= itemAmount;
    }
}
