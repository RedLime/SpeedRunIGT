package com.redlimerl.speedrunigt.mixins;

import com.mojang.authlib.GameProfile;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.ObtainItemCategoryCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
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
import org.spongepowered.include.com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

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

        // Custom Json category
        if (timer.getCategory().getConditionJson() != null) {
            List<ItemStack> itemStacks = Lists.newArrayList();
            itemStacks.addAll(this.getInventory().armor);
            itemStacks.addAll(this.getInventory().offHand);
            itemStacks.addAll(this.getInventory().main);
            for (CategoryCondition.Condition<?> condition : timer.getCustomCondition().getConditionList()) {
                if (condition instanceof ObtainItemCategoryCondition) {
                    timer.updateCondition((ObtainItemCategoryCondition) condition, itemStacks);
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
            if (this.getInventory().main.stream().filter(itemStack -> itemStack != null && itemStack != ItemStack.EMPTY).map(ItemStack::getItem).distinct().toArray().length == 36)
                InGameTimer.complete();
            return;
        }

        //All Workstations
        if (timer.getCategory() == RunCategories.ALL_WORKSTATIONS) {
            List<Item> items = this.getInventory().main.stream().map(ItemStack::getItem).collect(Collectors.toList());
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
            List<Item> items = this.getInventory().main.stream().map(ItemStack::getItem).collect(Collectors.toList());
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
            List<Item> items = this.getInventory().main.stream().map(ItemStack::getItem).collect(Collectors.toList());
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
            List<Item> items = this.getInventory().armor.stream().map(ItemStack::getItem).collect(Collectors.toList());
            if (items.contains(Items.IRON_HELMET) &&
                    items.contains(Items.IRON_CHESTPLATE) &&
                    items.contains(Items.IRON_BOOTS) &&
                    items.contains(Items.IRON_LEGGINGS) && experienceLevel >= 15) {
                InGameTimer.complete();
            }
        }

        //Stack of Lime Wool
        if (timer.getCategory() == RunCategories.STACK_OF_LIME_WOOL) {
            for (ItemStack itemStack : this.getInventory().main) {
                if (itemStack != null && itemStack.getItem() == Items.LIME_WOOL && itemStack.getCount() == 64) InGameTimer.complete();
            }
        }
    }


    private int portalTick = 0;
    @Inject(at = @At("HEAD"), method = "tick")
    public void updateNausea(CallbackInfo ci) {
        // Portal time update
        if (this.inNetherPortal) {
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
