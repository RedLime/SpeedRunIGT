package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerClientUtils;
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

        if (timer.getStatus() == TimerStatus.IDLE && !InGameTimerUtils.IS_CHANGING_DIMENSION && InGameTimerClientUtils.isFocusedClick() && (this.velocityX != 0 || this.velocityZ != 0 || this.jumping || this.isSneaking())) {
            timer.setPause(false, "moved player");
        }
        if (this.velocityX != 0 || this.velocityZ != 0 || this.jumping) {
            timer.updateFirstInput();
        }

        // Custom Json category
        if (timer.getCategory().getConditionJson() != null) {
            List<ItemStack> itemStacks = Lists.newArrayList();
            itemStacks.addAll(Lists.newArrayList(this.inventory.armor));
            itemStacks.addAll(Lists.newArrayList(this.inventory.main));
            for (CategoryCondition.Condition<?> condition : timer.getCustomCondition().getConditionList()) {
                if (condition instanceof ObtainItemCategoryCondition) {
                    timer.updateCondition((ObtainItemCategoryCondition) condition, itemStacks);
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
                if (itemStack == null || itemStack.isEmpty()) continue;
                String itemId = Item.getRawId(itemStack.getItem()) + (itemStack.isDamaged() ? (":" + itemStack.getMeta()) : "");
                if (!itemList.contains(itemId)) {
                    itemList.add(itemId);
                    slot++;
                }
            }
            if (slot == 36) InGameTimer.complete();
            return;
        }


        //All Swords
        if (timer.getCategory() == RunCategories.ALL_SWORDS) {
            List<Item> items = Arrays.stream(this.inventory.main).filter(itemStack -> itemStack != null && !itemStack.isEmpty()).map(ItemStack::getItem).collect(Collectors.toList());
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
            List<Item> items = Arrays.stream(this.inventory.main).filter(itemStack -> itemStack != null && !itemStack.isEmpty()).map(ItemStack::getItem).collect(Collectors.toList());
            if (items.contains(Items.COAL) &&
                    items.contains(Items.IRON_INGOT) &&
                    items.contains(Items.GOLD_INGOT) &&
                    items.contains(Items.DIAMOND) &&
                    items.contains(Items.REDSTONE) &&
                    items.contains(Items.EMERALD) &&
                    items.contains(Items.QUARTZ)
            ) {
                for (int i = 0; i < this.inventory.main.length; i++) {
                    ItemStack item = this.inventory.main[i];
                    if (item != null && !item.isEmpty() && item.getItem().equals(Items.DYE) && item.getMeta() == 4) {
                        InGameTimer.complete();
                    }
                }

            }
        }

        //Iron Armors & lvl 15
        if (timer.getCategory() == RunCategories.FULL_IA_15_LVL) {
            List<Item> items = Arrays.stream(this.inventory.armor).filter(itemStack -> itemStack != null && !itemStack.isEmpty()).map(ItemStack::getItem).collect(Collectors.toList());
            if (items.contains(Items.IRON_HELMET) &&
                    items.contains(Items.IRON_CHESTPLATE) &&
                    items.contains(Items.IRON_BOOTS) &&
                    items.contains(Items.IRON_LEGGINGS) && experienceLevel >= 15) {
                InGameTimer.complete();
            }
        }

        //Stack of Lime Wool
        if (timer.getCategory() == RunCategories.STACK_OF_LIME_WOOL) {
            for (ItemStack itemStack : this.inventory.main) {
                if (itemStack != null && itemStack.getItem() == Item.fromBlock(Blocks.WOOL) && itemStack.getDamage() == 5 && itemStack.count == 64) InGameTimer.complete();
            }
        }

        //For Timelines
        if (timer.getCategory() == RunCategories.ANY && this.y >= 100 && this.isSleeping())
            timer.tryInsertNewTimeline("sleep_on_tower");
    }


    private int portalTick = 0;
    @Inject(at = @At("HEAD"), method = "tickMovement")
    public void updateNausea(CallbackInfo ci) {
        // Portal time update
        if (this.changingDimension) {
            if (++portalTick >= 81 && !InGameTimerUtils.IS_CHANGING_DIMENSION) {
                portalTick = 0;
                if (InGameTimer.getInstance().getStatus() != TimerStatus.IDLE && client.isInSingleplayer()) {
                    InGameTimerUtils.IS_CHANGING_DIMENSION = true;
                    InGameTimer.getInstance().setPause(true, TimerStatus.IDLE, "portal ticks");
                }
            }
        } else {
            if (portalTick > 0 && InGameTimerUtils.IS_CHANGING_DIMENSION)
                InGameTimerUtils.IS_CHANGING_DIMENSION = false;
            portalTick = 0;
        }
    }
}
