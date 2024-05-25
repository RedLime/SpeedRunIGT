package com.redlimerl.speedrunigt.mixins.timeline;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.instance.GameInstance;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
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

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    @Unique
    private DimensionType oldDimension = null;
    @Unique
    private Vec3d lastPortalPos = null;

    @Inject(method = "moveToWorld", at = @At("HEAD"))
    public void onChangeDimension(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        oldDimension = this.getServerWorld().getDimension();
        this.lastPortalPos = this.getPos();
        InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = !InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY;
    }

    @Inject(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onPlayerChangeDimension(Lnet/minecraft/server/network/ServerPlayerEntity;)V", shift = At.Shift.AFTER))
    public void onChangedDimension(ServerWorld destination, CallbackInfoReturnable<Entity> cir, @Local TeleportTarget target) {
        DimensionType currentDimension = destination.getDimension();

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE) {
            if (oldDimension.isBedWorking() && currentDimension.hasCeiling()) {
                if (!timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY)
                    InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(World.NETHER, target.position.add(0, 0, 0), lastPortalPos.add(0, 0, 0));
            }

            if (oldDimension.hasCeiling() && currentDimension.isBedWorking()) {
                // doing this early, so we can use the portal pos list for the portal number
                int portalIndex = InGameTimerUtils.isBlindTraveled(this.lastPortalPos);
                boolean isNewPortal = InGameTimerUtils.isLoadableBlind(World.OVERWORLD, this.lastPortalPos.add(0, 0, 0), target.position.add(0, 0, 0));
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
        Set<Item> currentItemTypes = Stream.concat(this.getInventory().main.stream(), this.getInventory().offHand.stream()) // Go over both main inventory and offHand item list
                .filter(Objects::nonNull) // Remove nulls
                .map(ItemStack::getItem) // Turn each item stack into its item
                .collect(Collectors.toSet()); // Collect to a set of items that the player has
        return currentItemTypes.contains(Items.ENDER_EYE) || (currentItemTypes.contains(Items.ENDER_PEARL) && (currentItemTypes.contains(Items.BLAZE_ROD) || currentItemTypes.contains(Items.BLAZE_POWDER)));
    }
}
