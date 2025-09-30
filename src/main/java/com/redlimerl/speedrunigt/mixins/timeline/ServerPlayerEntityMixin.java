package com.redlimerl.speedrunigt.mixins.timeline;

import com.mojang.authlib.GameProfile;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.instance.GameInstance;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
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

    @Shadow public abstract ServerWorld getEntityWorld();

    @Unique
    private ServerWorld beforeWorld = null;
    @Unique
    private Vec3d lastPortalPos = null;

    public ServerPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/server/network/ServerPlayerEntity;", at = @At("HEAD"))
    public void onChangeDimension(TeleportTarget target, CallbackInfoReturnable<Entity> cir) {
        beforeWorld = this.getEntityWorld();
        lastPortalPos = this.getEntityPos();
        InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = !InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY;
    }

    @Inject(method = "teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/server/network/ServerPlayerEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onDimensionChanged(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.AFTER))
    public void onChangedDimension(TeleportTarget target, CallbackInfoReturnable<Entity> cir) {
        RegistryKey<World> oldRegistryKey = beforeWorld.getRegistryKey();
        RegistryKey<World> newRegistryKey = this.getEntityWorld().getRegistryKey();

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE) {
            if (oldRegistryKey == World.OVERWORLD && newRegistryKey == World.NETHER) {
                if (!timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY)
                    InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(World.NETHER, target.position().add(0, 0, 0), lastPortalPos.add(0, 0, 0));
            }

            if (oldRegistryKey == World.NETHER && newRegistryKey == World.OVERWORLD) {
                // doing this early, so we can use the portal pos list for the portal number
                int portalIndex = InGameTimerUtils.isBlindTraveled(this.lastPortalPos);
                boolean isNewPortal = InGameTimerUtils.isLoadableBlind(World.OVERWORLD, this.lastPortalPos.add(0, 0, 0), target.position().add(0, 0, 0));
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
        Set<Item> currentItemTypes = Stream.concat(this.getInventory().getMainStacks().stream(), Stream.of(this.getInventory().getStack(PlayerInventory.OFF_HAND_SLOT))) // Go over both main inventory and offHand item list
                .filter(Objects::nonNull) // Remove nulls
                .map(ItemStack::getItem) // Turn each item stack into its item
                .collect(Collectors.toSet()); // Collect to a set of items that the player has
        return currentItemTypes.contains(Items.ENDER_EYE) || (currentItemTypes.contains(Items.ENDER_PEARL) && (currentItemTypes.contains(Items.BLAZE_ROD) || currentItemTypes.contains(Items.BLAZE_POWDER)));
    }
}
