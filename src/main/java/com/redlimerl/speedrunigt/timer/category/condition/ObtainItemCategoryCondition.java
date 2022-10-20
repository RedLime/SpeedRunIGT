package com.redlimerl.speedrunigt.timer.category.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;

import java.util.List;
import java.util.Objects;

public class ObtainItemCategoryCondition extends CategoryCondition.Condition<List<ItemStack>> {

    private final String itemID;
    private final Integer itemDamage;
    private final int itemAmount;
    private final NbtCompound nbtTag;
    private final boolean strictMode;

    public ObtainItemCategoryCondition(JsonObject jsonObject) throws InvalidCategoryException {
        super(jsonObject);

        try {
            this.itemID = jsonObject.get("item_id").getAsString();
            this.itemAmount = jsonObject.has("item_amount") ? jsonObject.get("item_amount").getAsInt() : 1; // Optional
            this.itemDamage = jsonObject.has("item_damage") ? jsonObject.get("item_damage").getAsInt() : null; // Optional
            this.strictMode = !jsonObject.has("strict_mode") || jsonObject.get("strict_mode").getAsBoolean(); // Optional
            if (jsonObject.has("item_tag")) {
                JsonElement jsonElement = jsonObject.get("item_tag");
                this.nbtTag = StringNbtReader.parse(jsonElement.getAsString());
            } else {
                this.nbtTag = new NbtCompound();
            }
        } catch (Exception e) {
            throw new InvalidCategoryException(InvalidCategoryException.Reason.INVALID_JSON_DATA, "Failed to read condition \"" + this.getName() + "\"");
        }
    }

    @Override
    public boolean checkConditionComplete(List<ItemStack> itemStacks) {
        int amount = 0;

        for (ItemStack itemStack : itemStacks) {
            if (itemStack != null && Objects.equals(Item.getFromId(itemID), itemStack.getItem()) && (itemDamage == null || itemStack.getDamage() == itemDamage)) {
                if (!nbtTag.isEmpty()) {
                    if (itemStack.getNbt() == null) continue;
                    NbtCompound itemTag = itemStack.getNbt();
                    if (!itemTag.equals(nbtTag)) continue;
                }
                amount += itemStack.getCount();
            }
        }

        return amount >= itemAmount;
    }

    public boolean isStrictMode() {
        return strictMode;
    }
}
