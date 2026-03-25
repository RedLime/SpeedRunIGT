package com.redlimerl.speedrunigt.mixins.timeline;

import com.mojang.authlib.GameProfile;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.instance.GameInstance;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Shadow public abstract ServerLevel level();

    @Unique
    private ServerLevel beforeWorld = null;
    @Unique
    private Vec3 lastPortalPos = null;

    public ServerPlayerMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"))
    public void onChangeDimension(TeleportTransition target, CallbackInfoReturnable<Entity> cir) {
        beforeWorld = this.level();
        lastPortalPos = this.position();
        InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = !InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY;
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addDuringTeleport(Lnet/minecraft/world/entity/Entity;)V", shift = At.Shift.AFTER))
    public void onChangedDimension(TeleportTransition target, CallbackInfoReturnable<Entity> cir) {
        ResourceKey<Level> oldRegistryKey = beforeWorld.dimension();
        ResourceKey<Level> newRegistryKey = this.level().dimension();

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE) {
            if (oldRegistryKey == Level.OVERWORLD && newRegistryKey == Level.NETHER) {
                if (!timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY)
                    InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(Level.NETHER, target.position().add(0, 0, 0), lastPortalPos.add(0, 0, 0));
            }

            if (oldRegistryKey == Level.NETHER && newRegistryKey == Level.OVERWORLD) {
                // doing this early, so we can use the portal pos list for the portal number
                int portalIndex = InGameTimerUtils.isBlindTraveled(this.lastPortalPos);
                boolean isNewPortal = InGameTimerUtils.isLoadableBlind(Level.OVERWORLD, this.lastPortalPos.add(0, 0, 0), target.position().add(0, 0, 0));
                if (this.isEnoughTravel()) {
                    int portalNum = InGameTimerUtils.getPortalNumber(this.lastPortalPos);
                    SpeedRunIGT.debug("Portal number: " + portalNum);
                    GameInstance.getInstance().callEvents("nether_travel", factory -> factory.getDataValue("portal").equals(String.valueOf(portalNum)));
                    timer.tryInsertNewTimeline("nether_travel");
                    if (portalIndex == 0) {
                        timer.tryInsertNewTimeline("nether_travel_home");
                    } else {
                        timer.tryInsertNewTimeline("nether_travel_blind");
                    }
                }
                if (!timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY)
                    InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = isNewPortal;
            }
        }
    }

    @Unique
    private boolean isEnoughTravel() {
        Set<Item> currentItemTypes = Stream.concat(this.getInventory().getNonEquipmentItems().stream(), Stream.of(this.getInventory().getItem(Inventory.SLOT_OFFHAND))) // Go over both main inventory and offHand item list
                .filter(Objects::nonNull) // Remove nulls
                .map(ItemStack::getItem) // Turn each item stack into its item
                .collect(Collectors.toSet()); // Collect to a set of items that the player has
        return currentItemTypes.contains(Items.ENDER_EYE) || (currentItemTypes.contains(Items.ENDER_PEARL) && (currentItemTypes.contains(Items.BLAZE_ROD) || currentItemTypes.contains(Items.BLAZE_POWDER)));
    }
}
