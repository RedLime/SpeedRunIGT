package com.redlimerl.speedrunigt.mixins.timeline;

import com.mojang.authlib.GameProfile;
import com.redlimerl.speedrunigt.events.Event;
import com.redlimerl.speedrunigt.events.EventFactory;
import com.redlimerl.speedrunigt.events.EventFactoryLoader;
import com.redlimerl.speedrunigt.instance.GameInstance;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    @Shadow public abstract ServerWorld getServerWorld();

    public ServerPlayerEntityMixin(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    private ServerWorld beforeWorld = null;
    private Vec3d lastPortalPos = null;

    @Inject(method = "changeDimension", at = @At("HEAD"))
    public void onChangeDimension(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        this.beforeWorld = this.getServerWorld();
        this.lastPortalPos = this.getPos();
        InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = !InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY;
    }

    @Inject(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onPlayerChangeDimension(Lnet/minecraft/server/network/ServerPlayerEntity;)V", shift = At.Shift.AFTER))
    public void onChangedDimension(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        RegistryKey<World> oldRegistryKey = this.beforeWorld.getRegistryKey();
        RegistryKey<World> newRegistryKey = this.world.getRegistryKey();

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE) {
            if (oldRegistryKey == World.OVERWORLD && newRegistryKey == World.NETHER) {
                if (!timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY)
                    InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(World.NETHER, this.getPos().add(0, 0, 0), this.lastPortalPos.add(0, 0, 0));
            }

            if (oldRegistryKey == World.NETHER && newRegistryKey == World.OVERWORLD) {
                if (this.isEnoughTravel()) {
                    int portalIndex = InGameTimerUtils.isBlindTraveled(this.lastPortalPos);
                    InGameTimer.getInstance().tryInsertNewTimeline("nether_travel");
                    if (portalIndex == 0) {
                        InGameTimer.getInstance().tryInsertNewTimeline("nether_travel_home");
                    } else {
                        InGameTimer.getInstance().tryInsertNewTimeline("nether_travel_blind");
                    }
                }
                if (!timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY)
                    InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(World.OVERWORLD, this.lastPortalPos.add(0, 0, 0), this.getPos().add(0, 0, 0));
            }
        }
    }

    @Unique
    private boolean isEnoughTravel() {
        boolean eye = false, pearl = false, rod = false;
        for (ItemStack itemStack : this.inventory.main) {
            if (itemStack != null) {
                if (itemStack.getItem() == Items.ENDER_EYE) eye = true;
                if (itemStack.getItem() == Items.ENDER_PEARL) pearl = true;
                if (itemStack.getItem() == Items.BLAZE_POWDER || itemStack.getItem() == Items.BLAZE_ROD) rod = true;
            }
        }

        return eye || (pearl && rod);
    }
}
