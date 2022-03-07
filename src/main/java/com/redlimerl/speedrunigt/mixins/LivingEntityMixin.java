package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow protected boolean dead;

    @Shadow private @Nullable LivingEntity attacker;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        @NotNull InGameTimer timer = InGameTimer.getInstance();

        if (this.removed || this.dead || timer.getStatus() == TimerStatus.NONE || !(this.attacker instanceof PlayerEntity)) return;

        //Kill All Bosses
        if (timer.getCategory() == RunCategories.KILL_ALL_BOSSES) {
            if (this.getType() == EntityType.ENDER_DRAGON) {
                timer.updateMoreData(0, 1);
            }
            if (this.getType() == EntityType.WITHER) {
                timer.updateMoreData(1, 1);
                RunCategories.checkAllBossesCompleted();
            }
            if (this.getType() == EntityType.ELDER_GUARDIAN) {
                timer.updateMoreData(2, 1);
                RunCategories.checkAllBossesCompleted();
            }
        }

        //Kill Wither
        if (timer.getCategory() == RunCategories.KILL_WITHER && this.getType() == EntityType.WITHER) {
            InGameTimer.complete();
        }

        //Kill Elder Guardian
        if (timer.getCategory() == RunCategories.KILL_ELDER_GUARDIAN && this.getType() == EntityType.ELDER_GUARDIAN) {
            InGameTimer.complete();
        }

        // For Timelines
        if (timer.getCategory() == RunCategories.KILL_ALL_BOSSES) {
            if (this.getType() == EntityType.WITHER) timer.tryInsertNewTimeline("kill_wither");
            if (this.getType() == EntityType.ELDER_GUARDIAN) timer.tryInsertNewTimeline("kill_elder_guardian");
            if (this.getType() == EntityType.ENDER_DRAGON) timer.tryInsertNewTimeline("kill_ender_dragon");
        }
    }
}
