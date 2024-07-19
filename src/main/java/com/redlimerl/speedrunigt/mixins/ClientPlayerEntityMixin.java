package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Lists;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerClientUtils;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.ObtainItemCategoryCondition;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityMixin(World world, String string) {
        super(world, string);
    }

    @Shadow public abstract boolean isSneaking();
    @Shadow protected MinecraftClient client;



    @Inject(method = "tickMovement",
            at = @At("TAIL"))
    private void onMove(CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.NONE || timer.getStatus() == TimerStatus.COMPLETED_LEGACY) return;

        if (timer.getStatus() == TimerStatus.IDLE && !InGameTimerUtils.IS_CHANGING_DIMENSION && InGameTimerClientUtils.isFocusedClick() && (this.velocityX != 0 || this.velocityZ != 0 || this.jumping || this.isSneaking())) {
            timer.setPause(false, "moved player");
        }
        if (this.velocityX != 0 || this.velocityZ != 0 || this.jumping) {
            timer.updateFirstInput();
        }

        List<ItemStack> playerItemList = Lists.newArrayList();
        playerItemList.addAll(Lists.newArrayList(this.inventory.armor));
        playerItemList.addAll(Lists.newArrayList(this.inventory.main));

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
            for (int i = 0; i < this.inventory.main.length; i++) {
                ItemStack itemStack = this.inventory.main[i];
                if (itemStack == null || itemStack.isDamaged() || itemStack.getItem() == null) continue;
                String itemId = itemStack.getItem().id + (itemStack.isStackable() ? (":" + itemStack.getData()) : "");
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
                if (itemStack.getItem() == Item.ITEMS[Block.WOOL.id] && itemStack.getDamage() == 5 && itemStack.count == 64) InGameTimer.complete();
            }

            if (itemStack.getItem() == Item.BOOK) timer.tryInsertNewTimeline("pickup_book");
        }

        List<Item> items = Arrays.stream(this.inventory.main).filter(itemStack -> itemStack != null && !itemStack.isDamaged()).map(ItemStack::getItem).collect(Collectors.toList());
        List<Item> armors = Arrays.stream(this.inventory.armor).filter(itemStack -> itemStack != null && !itemStack.isDamaged()).map(ItemStack::getItem).collect(Collectors.toList());

        //All Swords
        if (timer.getCategory() == RunCategories.ALL_SWORDS) {
            if (items.contains(Item.STONE_SWORD) &&
                    items.contains(Item.DIAMOND_SWORD) &&
                    items.contains(Item.GOLD_SWORD) &&
                    items.contains(Item.IRON_SWORD) &&
                    items.contains(Item.WOOD_SWORD)) {
                InGameTimer.complete();
            }
        }

        //All Minerals
        if (timer.getCategory() == RunCategories.ALL_MINERALS) {
            if (items.contains(Item.COAL) &&
                    items.contains(Item.IRON_INGOT) &&
                    items.contains(Item.GOLD_INGOT) &&
                    items.contains(Item.DIAMOND) &&
                    items.contains(Item.REDSTONE) &&
                    items.contains(Item.EMERALD) &&
                    items.contains(Item.NETHER_QUARTZ)
            ) {
                for (int i = 0; i < this.inventory.main.length; i++) {
                    ItemStack item = this.inventory.main[i];
                    if (item != null && !item.isDamaged() && item.getItem().equals(Item.DYES) && item.getData() == 4) {
                        InGameTimer.complete();
                    }
                }

            }
        }

        //Iron Armors & lvl 15
        if (timer.getCategory() == RunCategories.FULL_IA_15_LVL) {
            if (armors.contains(Item.IRON_HELMET) &&
                    armors.contains(Item.IRON_CHESTPLATE) &&
                    armors.contains(Item.IRON_BOOTS) &&
                    armors.contains(Item.IRON_LEGGINGS) && this.experienceLevel >= 15) {
                InGameTimer.complete();
            }
        }

        // This is flawed as the most recent towers do a strat called "bunk bed" where the player spawns well below y 100
        // For Timelines
        // if (this.y >= 100 && this.isSleeping())
        //     timer.tryInsertNewTimeline("sleep_on_tower");
    }


    private Long latestPortalEnter = null;
    private int portalTick = 0;
    @Inject(at = @At("HEAD"), method = "tickMovement")
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
            if (this.latestPortalEnter != null) {
                InGameTimer.getInstance().tryExcludeIGT(System.currentTimeMillis() - this.latestPortalEnter, "nether portal lag");
                this.latestPortalEnter = null;
            }
            this.portalTick = 0;
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
