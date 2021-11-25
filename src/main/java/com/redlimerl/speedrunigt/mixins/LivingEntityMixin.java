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

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        @NotNull InGameTimer timer = InGameTimer.getInstance();

        if (this.removed || this.dead || timer.getStatus() == TimerStatus.NONE) return;

        //Kill All Bosses
        if (timer.getCategory() == RunCategory.KILL_ALL_BOSSES) {
            if (this.getType() == EntityType.WITHER) {
                timer.updateMoreData(1, 1);
            }
            if (this.getType() == EntityType.ELDER_GUARDIAN) {
                timer.updateMoreData(2, 1);
            }
            if (timer.getMoreData(0) == 1 && timer.getMoreData(1) == 1 && timer.getMoreData(2) == 1)
                timer.complete();
            return;
        }

        //Kill Wither
        if (timer.getCategory() == RunCategory.KILL_WITHER && this.getType() == EntityType.WITHER) {
            timer.complete();
            return;
        }

        //Kill Elder Guardian
        if (timer.getCategory() == RunCategory.KILL_ELDER_GUARDIAN && this.getType() == EntityType.ELDER_GUARDIAN) {
            timer.complete();
        }
    }
}
