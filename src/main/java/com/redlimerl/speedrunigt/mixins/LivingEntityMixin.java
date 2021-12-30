package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.RunCategory;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow protected boolean dead;


    public LivingEntityMixin(World world) {
        super(world);
    }
    /**
     * @author Void_X_Walker
     * @reason Backported to 1.8
     */

    @Inject(at = @At("HEAD"), method = "onKilled")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        @NotNull InGameTimer timer = InGameTimer.getInstance();

        if (this.removed || this.dead || timer.getStatus() == TimerStatus.NONE) return;

        //Kill All Bosses
        if (timer.getCategory() == RunCategory.KILL_ALL_BOSSES) {
            if (EntityType.getIdByEntity(this) == 64) {//Wither
                timer.updateMoreData(1, 1);
            }
            if (timer.getMoreData(0) == 1 && timer.getMoreData(1) == 1)
                InGameTimer.complete();
            return;
        }

        //Kill Wither
        if (timer.getCategory() == RunCategory.KILL_WITHER && EntityType.getIdByEntity(this) == 64) {
            InGameTimer.complete();
        }
    }
}
