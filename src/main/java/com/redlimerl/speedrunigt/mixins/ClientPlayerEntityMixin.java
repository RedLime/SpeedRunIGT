package com.redlimerl.speedrunigt.mixins;

import com.mojang.authlib.GameProfile;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffect;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow public abstract boolean isSneaking();
    @Shadow protected MinecraftClient client;

    @Shadow public float timeInPortal;

    /**
     * @author Void_X_Walker
     * @reason Backported to 1.8, removed non 1.8 categories
     */
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tickMovement",
            at = @At("TAIL"))
    private void onMove(CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() == TimerStatus.IDLE && (this.velocityX != 0 || this.velocityZ != 0 || this.jumping||this.isSneaking())) {
            timer.setPause(false);
        }
        if (this.velocityX != 0 || this.velocityZ != 0 || this.jumping) {
            timer.updateFirstInput();
        }

        if (timer.getStatus() == TimerStatus.NONE || timer.getStatus() == TimerStatus.COMPLETED_LEGACY) return;

        //HIGH%
        if (timer.getCategory() == RunCategory.HIGH && this.y >= 420) {
            InGameTimer.complete();
            return;
        }

        //Full Inventory
        if (timer.getCategory() == RunCategory.FULL_INV) {
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
        if (timer.getCategory() == RunCategory.ALL_SWORDS) {
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
        if (timer.getCategory() == RunCategory.ALL_MINERALS) {
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
                    if (item != null && !item.isEmpty() && item.getItem().equals(Items.DYE) && DyeColor.getById(item.getMeta()) == DyeColor.BLUE) {
                        InGameTimer.complete();
                    }
                }

            }
        }

        //Iron Armors & lvl 15
        if (timer.getCategory() == RunCategory.FULL_IA_15_LVL) {
            List<Item> items = Arrays.stream(this.inventory.armor).filter(itemStack -> itemStack != null && !itemStack.isEmpty()).map(ItemStack::getItem).collect(Collectors.toList());
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
       if(this.changingDimension && this.timeInPortal == 1 && client.isInSingleplayer()){
            InGameTimer.checkingWorld = false;
            InGameTimer.getInstance().setPause(true, TimerStatus.IDLE);
        }
    }
}
