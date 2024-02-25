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
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
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

        if (timer.getStatus() == TimerStatus.IDLE && !InGameTimerUtils.IS_CHANGING_DIMENSION && (this.field_5967 != 0 || this.field_6006 != 0 || this.jumping || this.isSneaking())) {
            timer.setPause(false, "moved player");
        }
        if (this.field_5967 != 0 || this.field_6006 != 0 || this.jumping || this.isSneaking()) {
            timer.updateFirstInput();
        }

        List<ItemStack> playerItemList = Lists.newArrayList();
        playerItemList.addAll(this.inventory.main);
        playerItemList.addAll(this.inventory.armor);
        playerItemList.addAll(this.inventory.offHand);

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
            for (int i = 0; i < this.inventory.main.size(); i++) {
                ItemStack itemStack = this.inventory.main.get(i);
                if (itemStack.isDamaged() || itemStack.getItem() == Items.AIR) continue;
                String itemId = Item.getRawId(itemStack.getItem()) + (itemStack.isStackable() ? (":" + itemStack.method_0_8356()) : "");
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
                if (Item.fromBlock(Blocks.WOOL).equals(itemStack.getItem()) && itemStack.method_0_8356() == 5 && itemStack.getCount() == 64) InGameTimer.complete();
            }
        }

        List<Item> items = this.inventory.main.stream().map(ItemStack::getItem).collect(Collectors.toList());
        List<Item> armors = this.inventory.armor.stream().map(ItemStack::getItem).collect(Collectors.toList());

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
                for (int i = 0; i < this.inventory.main.size(); i++) {
                    ItemStack item = this.inventory.main.get(i);
                    if (!item.isDamaged() && item.getItem().equals(Items.DYE) && DyeColor.method_0_8193(item.method_0_8356()) == DyeColor.BLUE) {
                        InGameTimer.complete();
                    }
                }

            }
        }

        //Iron Armors & lvl 15
        if (timer.getCategory() == RunCategories.FULL_IA_15_LVL) {
            if (armors.contains(Items.field_8743) &&
                    armors.contains(Items.field_8523) &&
                    armors.contains(Items.field_8660) &&
                    armors.contains(Items.field_8396) && experienceLevel >= 15) {
                InGameTimer.complete();
            }
        }
    }


    private Long latestPortalEnter = null;
    private int portalTick = 0;
    @Inject(at = @At("HEAD"), method = "method_3070")
    public void updateNausea(CallbackInfo ci) {
        // Portal time update
        if (this.inNetherPortal) {
            if (++portalTick >= 81 && !InGameTimerUtils.IS_CHANGING_DIMENSION) {
                portalTick = 0;
                if (InGameTimer.getInstance().getStatus() != TimerStatus.IDLE && client.isIntegratedServerRunning()) {
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
    public void method_5872(float cursorDeltaX, float cursorDeltaY) {
        super.method_5872(cursorDeltaX, cursorDeltaY);

        if (cursorDeltaX != 0 || cursorDeltaY != 0) {
            InGameTimer timer = InGameTimer.getInstance();
            if (timer.getStatus() == TimerStatus.IDLE && !InGameTimerUtils.IS_CHANGING_DIMENSION) {
                timer.setPause(false, "changed look direction");
            }
        }
    }
}
