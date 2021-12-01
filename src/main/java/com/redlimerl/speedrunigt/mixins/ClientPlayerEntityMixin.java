package com.redlimerl.speedrunigt.mixins;

import com.mojang.authlib.GameProfile;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow public float nextNauseaStrength;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
            at = @At("TAIL"))
    private void onMove(MovementType movementType, Vec3d vec3d, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.IDLE && (vec3d.x != 0 || vec3d.z != 0 || this.jumping)) {
            timer.setPause(false);
        }
        if (vec3d.x != 0 || vec3d.z != 0 || this.jumping) {
            timer.updateFirstInput();
        }
        
        if (timer.getStatus() == TimerStatus.NONE || timer.getStatus() == TimerStatus.COMPLETED) return;

        //HIGH%
        if (timer.getCategory() == RunCategory.HIGH && this.getY() >= 420) {
            InGameTimer.complete();
            return;
        }

        //Full Inventory
        if (timer.getCategory() == RunCategory.FULL_INV) {
            if (this.inventory.main.stream().filter(itemStack -> itemStack != null && itemStack != ItemStack.EMPTY).map(ItemStack::getItem).distinct().toArray().length == 36)
                InGameTimer.complete();
            return;
        }

        //All Workstations
        if (timer.getCategory() == RunCategory.ALL_WORKSTATIONS) {
            List<Item> items = this.inventory.main.stream().map(ItemStack::getItem).collect(Collectors.toList());
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
        if (timer.getCategory() == RunCategory.ALL_SWORDS) {
            List<Item> items = this.inventory.main.stream().map(ItemStack::getItem).collect(Collectors.toList());
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
        if (timer.getCategory() == RunCategory.ALL_MINERALS) {
            List<Item> items = this.inventory.main.stream().map(ItemStack::getItem).collect(Collectors.toList());
            if (items.contains(Items.COAL) &&
                    items.contains(Items.IRON_INGOT) &&
                    items.contains(Items.GOLD_INGOT) &&
                    items.contains(Items.DIAMOND) &&
                    items.contains(Items.REDSTONE) &&
                    items.contains(Items.LAPIS_LAZULI) &&
                    items.contains(Items.EMERALD) &&
                    items.contains(Items.QUARTZ)) {
                InGameTimer.complete();
            }
        }

        //Iron Armors & lvl 15
        if (timer.getCategory() == RunCategory.FULL_IA_15_LVL) {
            List<Item> items = this.inventory.armor.stream().map(ItemStack::getItem).collect(Collectors.toList());
            if (items.contains(Items.IRON_HELMET) &&
                    items.contains(Items.IRON_CHESTPLATE) &&
                    items.contains(Items.IRON_BOOTS) &&
                    items.contains(Items.IRON_LEGGINGS) && experienceLevel >= 15) {
                InGameTimer.complete();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void updateNausea(CallbackInfo ci) {
        if (this.inNetherPortal && nextNauseaStrength + 0.0125F >= 1F && InGameTimer.getInstance().getStatus() != TimerStatus.IDLE) {
            InGameTimer.checkingWorld = false;
            InGameTimer.getInstance().setPause(true, TimerStatus.IDLE);
        }
    }
}
