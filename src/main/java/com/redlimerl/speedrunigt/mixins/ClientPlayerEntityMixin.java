package com.redlimerl.speedrunigt.mixins;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.ObtainItemCategoryCondition;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow @Final protected MinecraftClient client;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
            at = @At("TAIL"))
    private void onMove(MovementType movementType, Vec3d vec3d, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.NONE || timer.getStatus() == TimerStatus.COMPLETED_LEGACY) return;

        if (timer.getStatus() == TimerStatus.IDLE && !InGameTimerUtils.IS_CHANGING_DIMENSION && (vec3d.x != 0 || vec3d.z != 0 || this.jumping || this.isSneaking())) {
            timer.setPause(false, "moved player");
        }
        if (vec3d.x != 0 || vec3d.z != 0 || this.jumping) {
            timer.updateFirstInput();
        }

        List<ItemStack> playerItemList = Lists.newArrayList();
        playerItemList.addAll(this.getInventory().getMainStacks());
        for (Integer slotId : PlayerInventory.EQUIPMENT_SLOTS.keySet()) {
            playerItemList.add(this.getInventory().getStack(slotId));
        }

        // Custom Json category
        if (timer.getCategory().getConditionJson() != null) {
            for (CategoryCondition.Conditions conditions : timer.getCustomCondition().map(CategoryCondition::getConditions).orElse(Lists.newArrayList())) {
                int strict = 0;
                for (CategoryCondition.Condition<?> condition : conditions.getConditions()) {
                    if (condition instanceof ObtainItemCategoryCondition obtainItemCondition) {
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
                        if (condition instanceof ObtainItemCategoryCondition obtainItemCondition) {
                            timer.updateCondition(obtainItemCondition, playerItemList);
                        }
                    }
                }

            }
            timer.checkConditions();
        }

        //HIGH%
        if (timer.getCategory() == RunCategories.HIGH && this.getY() >= 420) {
            InGameTimer.complete();
            return;
        }

        //Full Inventory
        if (timer.getCategory() == RunCategories.FULL_INV) {
            if (this.getInventory().getMainStacks().stream().filter(itemStack -> itemStack != null && itemStack != ItemStack.EMPTY && itemStack.getItem() != Items.AIR).map(ItemStack::getItem).distinct().toArray().length == 36)
                InGameTimer.complete();
            return;
        }

        for (ItemStack itemStack : playerItemList) {
            int shells = 0;

            if (itemStack == null) continue;

            // Timelines
            if (itemStack.getItem() == Items.TRIDENT) {
                timer.tryInsertNewTimeline("got_trident");
            }
            if (itemStack.getItem() == Items.NAUTILUS_SHELL) {
                shells += itemStack.getCount();
            }
            if (itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock() instanceof ShulkerBoxBlock) {
                shells += InGameTimerUtils.getItemCountFromShulkerBox(this.getEntityWorld(), itemStack, Items.NAUTILUS_SHELL);
            }
            if (shells > timer.getMoreData(1541)) {
                int i = 1;
                while (shells >= timer.getMoreData(1541) + i) {
                    timer.tryInsertNewTimeline("got_shell_" + (timer.getMoreData(1541) + i++));
                }
                timer.updateMoreData(1541, shells);
            }



            //Stack of Lime Wool
            if (timer.getCategory() == RunCategories.STACK_OF_LIME_WOOL) {
                if (itemStack.getItem() == Items.LIME_WOOL && itemStack.getCount() == 64) InGameTimer.complete();
            }
        }

        List<Item> items = this.getInventory().getMainStacks().stream().map(ItemStack::getItem).toList();
        List<Item> armors = new ArrayList<>();
        for (Int2ObjectMap.Entry<EquipmentSlot> entry : PlayerInventory.EQUIPMENT_SLOTS.int2ObjectEntrySet()) {
            if (entry.getValue().getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                armors.add(this.getInventory().getStack(entry.getIntKey()).getItem());
            }
        }

        //All Workstations
        if (timer.getCategory() == RunCategories.ALL_WORKSTATIONS) {
            if (items.contains(Items.BLAST_FURNACE) &&
                    items.contains(Items.SMOKER) &&
                    items.contains(Items.CARTOGRAPHY_TABLE) &&
                    items.contains(Items.BREWING_STAND) &&
                    items.contains(Items.COMPOSTER) &&
                    items.contains(Items.BARREL) &&
                    items.contains(Items.FLETCHING_TABLE) &&
                    items.contains(Items.CAULDRON) &&
                    items.contains(Items.LECTERN) &&
                    items.contains(Items.STONECUTTER) &&
                    items.contains(Items.LOOM) &&
                    items.contains(Items.SMITHING_TABLE) &&
                    items.contains(Items.GRINDSTONE)) {
                InGameTimer.complete();
            }
        }

        //All Swords
        if (timer.getCategory() == RunCategories.ALL_SWORDS) {
            if (items.contains(Items.STONE_SWORD) &&
                    items.contains(Items.DIAMOND_SWORD) &&
                    items.contains(Items.GOLDEN_SWORD) &&
                    items.contains(Items.IRON_SWORD) &&
                    items.contains(Items.NETHERITE_SWORD) &&
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
                    items.contains(Items.LAPIS_LAZULI) &&
                    items.contains(Items.EMERALD) &&
                    items.contains(Items.QUARTZ) &&
                    items.contains(Items.NETHERITE_INGOT) &&
                    items.contains(Items.COPPER_INGOT) &&
                    items.contains(Items.AMETHYST_SHARD)) {
                InGameTimer.complete();
            }
        }

        //Iron Armors & lvl 15
        if (timer.getCategory() == RunCategories.FULL_IA_15_LVL) {
            if (armors.contains(Items.IRON_HELMET) &&
                    armors.contains(Items.IRON_CHESTPLATE) &&
                    armors.contains(Items.IRON_BOOTS) &&
                    armors.contains(Items.IRON_LEGGINGS) && this.experienceLevel >= 15) {
                InGameTimer.complete();
            }
        }
    }


    private Long latestPortalEnter = null;
    private int portalTick = 0;
    @Inject(at = @At("HEAD"), method = "tick")
    public void updateNausea(CallbackInfo ci) {
        // Portal time update
        if (this.portalManager != null && this.portalManager.isInPortal()) {
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
    public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
        super.changeLookDirection(cursorDeltaX, cursorDeltaY);

        if (cursorDeltaX != 0 || cursorDeltaY != 0) {
            InGameTimer timer = InGameTimer.getInstance();
            if (timer.getStatus() == TimerStatus.IDLE && !InGameTimerUtils.IS_CHANGING_DIMENSION) {
                timer.setPause(false, "changed look direction");
            }
        }
    }
}
