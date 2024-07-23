package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.instance.GameInstance;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.dimension.TheNetherDimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(PlayerManager.class)
public abstract class ServerPlayerEntityMixin {

    private ServerWorld beforeWorld = null;
    private Vec3d lastPortalPos = null;

    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void onRespawnPlayer(ServerPlayerEntity dimension, int alive, boolean par3, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        if (cir.getReturnValue().y > 150 && cir.getReturnValue().method_3186() /*gets respawn point, null if using world spawn*/ != null) {
            InGameTimer.getInstance().tryInsertNewTimeline("tower_start");
        }
    }

    @Inject(method = "teleportToDimension", at = @At("HEAD"))
    public void onChangeDimension(ServerPlayerEntity player, int dimension, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        beforeWorld = player.getServerWorld();
        lastPortalPos = Vec3d.method_604(player.x, player.y, player.z);
        InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = !InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY;

        //All Portals
        SpeedRunIGT.debug("Current portals : " + timer.getEndPortalPosList().size());
        if (InGameTimerUtils.IS_KILLED_ENDER_DRAGON && timer.getCategory() == RunCategories.ALL_PORTALS && timer.getEndPortalPosList().size() == 3) {
            InGameTimer.complete();
        }
    }

    @Inject(method = "teleportToDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;method_1986(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER))
    public void onChangedDimension(ServerPlayerEntity player, int dimension, CallbackInfo ci) {
        Dimension oldDimension = beforeWorld.dimension;
        Dimension newDimension = player.world.dimension;

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE) {
            if (oldDimension instanceof OverworldDimension && newDimension instanceof TheNetherDimension) {
                if (!timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY) InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(newDimension, Vec3d.method_604(player.x, player.y, player.z), lastPortalPos.method_613(0, 0, 0));
            }

            if (oldDimension instanceof TheNetherDimension && newDimension instanceof OverworldDimension) {
                // doing this early, so we can use the portal pos list for the portal number
                int portalIndex = InGameTimerUtils.isBlindTraveled(this.lastPortalPos);
                boolean isNewPortal = InGameTimerUtils.isLoadableBlind(newDimension, lastPortalPos.method_613(0, 0, 0), Vec3d.method_604(player.x, player.y, player.z));;
                if (this.isEnoughTravel(player)) {
                    int portalNum = InGameTimerUtils.getPortalNumber(this.lastPortalPos);
                    GameInstance.getInstance().callEvents("nether_travel", factory -> factory.getDataValue("portal").equals(String.valueOf(portalNum)));
                    InGameTimer.getInstance().tryInsertNewTimeline("nether_travel");
                    if (portalIndex == 0) {
                        InGameTimer.getInstance().tryInsertNewTimeline("nether_travel_home");
                    } else {
                        timer.tryInsertNewTimeline("nether_travel_blind");
                    }
                if (!timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY) InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = isNewPortal;
                }
            }
        }
    }

    @Unique
    private boolean isEnoughTravel(ServerPlayerEntity serverPlayerEntity) {
        Set<Item> currentItemTypes = Arrays.stream(serverPlayerEntity.inventory.main) // Go over both main inventory item list
                .filter(Objects::nonNull) // Remove nulls
                .map(ItemStack::getItem) // Turn each item stack into its item
                .collect(Collectors.toSet()); // Collect to a set of items that the player has
        return currentItemTypes.contains(Item.EYE_OF_ENDER) || currentItemTypes.contains(Item.BLAZE_ROD) || currentItemTypes.contains(Item.BLAZE_POWDER);
    }
}
