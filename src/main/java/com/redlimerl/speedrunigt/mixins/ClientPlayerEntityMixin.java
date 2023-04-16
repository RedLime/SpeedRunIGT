package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.ObtainItemCategoryCondition;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow public abstract boolean isSneaking();
    @Shadow protected MinecraftClient client;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tickMovement",
            at = @At("TAIL"))
    private void onMove(CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.NONE || timer.getStatus() == TimerStatus.COMPLETED_LEGACY) return;

        if (timer.getStatus() == TimerStatus.IDLE && !InGameTimerUtils.IS_CHANGING_DIMENSION && (this.velocityX != 0 || this.velocityZ != 0 || this.jumping || this.isSneaking())) {
            timer.setPause(false, "moved player");
        }
        if (this.velocityX != 0 || this.velocityZ != 0 || this.jumping || this.isSneaking()) {
            timer.updateFirstInput();
        }

        List<ItemStack> playerItemList = Lists.newArrayList();
        playerItemList.addAll(this.inventory.field_15082);
        playerItemList.addAll(this.inventory.field_15083);
        playerItemList.addAll(this.inventory.field_15084);

        // Custom Json category
        if (timer.getCategory().getConditionJson() != null) {
            for (CategoryCondition.Conditions conditions : timer.getCustomCondition().map(CategoryCondition::getConditions).orElse(Lists.newArrayList())) {
                int strict = 0;
                for (CategoryCondition.Condition<?> condition : conditions.getConditions()) {
                    if (condition instanceof ObtainItemCategoryCondition) {
                        ObtainItemCategoryCondition obtainItemCondition = (ObtainItemCategoryCondition) condition;
                        boolean canComplete = obtainItemCondition.checkConditionComplete(playerItemList);
                        if (obtainItemCondition.isStrictMode()) {
                            if (!canComplete) strict++;
                        } else {
                            timer.updateCondition(obtainItemCondition, playerItemList);
                        }
                    }
                }

                if (strict == 0) {
                    for (CategoryCondition.Condition<?> condition : conditions.getConditions()) {
                        if (condition instanceof ObtainItemCategoryCondition) {
                            ObtainItemCategoryCondition obtainItemCondition = (ObtainItemCategoryCondition) condition;
                            timer.updateCondition(obtainItemCondition, playerItemList);
                        }
                    }
                }

            }
            timer.checkConditions();
        }

        //HIGH%
        if (timer.getCategory() == RunCategories.HIGH && this.y >= 420) {
            InGameTimer.complete();
            return;
        }

        //Full Inventory
        if (timer.getCategory() == RunCategories.FULL_INV) {
            ArrayList<String> itemList = new ArrayList<>();
            int slot = 0;
            for (int i = 0; i < this.inventory.field_15082.size(); i++) {
                ItemStack itemStack = this.inventory.field_15082.get(i);
                if (itemStack.isEmpty() || itemStack.getItem() == Items.AIR) continue;
                String itemId = Item.getRawId(itemStack.getItem()) + (itemStack.isDamaged() ? (":" + itemStack.getMeta()) : "");
                if (!itemList.contains(itemId)) {
                    itemList.add(itemId);
                    slot++;
                }
            }
            if (slot == 36) InGameTimer.complete();
            return;
        }

        for (ItemStack itemStack : playerItemList) {
            if (itemStack == null) continue;

            //Stack of Lime Wool
            if (timer.getCategory() == RunCategories.STACK_OF_LIME_WOOL) {
                if (Item.fromBlock(Blocks.WOOL).equals(itemStack.getItem()) && itemStack.getMeta() == 5 && itemStack.getCount() == 64) InGameTimer.complete();
            }
        }

        List<Item> items = this.inventory.field_15082.stream().map(ItemStack::getItem).collect(Collectors.toList());
        List<Item> armors = this.inventory.field_15083.stream().map(ItemStack::getItem).collect(Collectors.toList());

        //All Swords
        if (timer.getCategory() == RunCategories.ALL_SWORDS) {
            if (items.contains(Items.STONE_SWORD) &&
                    items.contains(Items.DIAMOND_SWORD) &&
                    items.contains(Items.GOLDEN_SWORD) &&
                    items.contains(Items.IRON_SWORD) &&
                    items.contains(Items.WOODEN_SWORD)) {
                InGameTimer.complete();
            }
        }

        //All Minerals
        if (timer.getCategory() == RunCategories.ALL_MINERALS) {
            if (items.contains(Items.COAL) &&
                    items.contains(Items.IRON_INGOT) &&
                    items.contains(Items.GOLD_INGOT) &&
                    items.contains(Items.DIAMOND) &&
                    items.contains(Items.REDSTONE) &&
                    items.contains(Items.EMERALD) &&
                    items.contains(Items.QUARTZ)
            ) {
                for (int i = 0; i < this.inventory.field_15082.size(); i++) {
                    ItemStack item = this.inventory.field_15082.get(i);
                    if (!item.isEmpty() && item.getItem().equals(Items.DYE) && DyeColor.getById(item.getMeta()) == DyeColor.BLUE) {
                        InGameTimer.complete();
                    }
                }

            }
        }

        //Iron Armors & lvl 15
        if (timer.getCategory() == RunCategories.FULL_IA_15_LVL) {
            if (armors.contains(Items.IRON_HELMET) &&
                    armors.contains(Items.IRON_CHESTPLATE) &&
                    armors.contains(Items.IRON_BOOTS) &&
                    armors.contains(Items.IRON_LEGGINGS) && experienceLevel >= 15) {
                InGameTimer.complete();
            }
        }
    }


    private Long latestPortalEnter = null;
    private int portalTick = 0;
    @Inject(at = @At("HEAD"), method = "tick")
    public void updateNausea(CallbackInfo ci) {
        // Portal time update
        if (this.changingDimension) {
            if (++portalTick >= 81 && !InGameTimerUtils.IS_CHANGING_DIMENSION) {
                portalTick = 0;
                if (InGameTimer.getInstance().getStatus() != TimerStatus.IDLE && client.isInSingleplayer()) {
                    latestPortalEnter = System.currentTimeMillis();
                }
            }
        } else {
            if (latestPortalEnter != null) {
                InGameTimer.getInstance().tryExcludeIGT(System.currentTimeMillis() - latestPortalEnter, "nether portal lag");
                latestPortalEnter = null;
            }
            portalTick = 0;
        }
    }

    @Override
    public void increaseTransforms(float cursorDeltaX, float cursorDeltaY) {
        super.increaseTransforms(cursorDeltaX, cursorDeltaY);

        if (cursorDeltaX != 0 || cursorDeltaY != 0) {
            InGameTimer timer = InGameTimer.getInstance();
            if (timer.getStatus() == TimerStatus.IDLE && !InGameTimerUtils.IS_CHANGING_DIMENSION) {
                timer.setPause(false, "changed look direction");
            }
        }
    }
}
