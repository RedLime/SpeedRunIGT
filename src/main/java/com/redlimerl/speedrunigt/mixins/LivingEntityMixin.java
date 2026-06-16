package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

    @Shadow @Nullable protected EntityReference<Player> lastHurtByPlayer;

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "die")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        @NotNull InGameTimer timer = InGameTimer.getInstance();

        if (this.isRemoved() || this.dead || timer.getStatus() == TimerStatus.NONE) return;

        // For Timelines
        if (this.getType() == EntityTypes.WITHER && this.lastHurtByPlayer != null) timer.tryInsertNewTimeline("kill_wither");
        if (this.getType() == EntityTypes.ELDER_GUARDIAN && this.lastHurtByPlayer != null) timer.tryInsertNewTimeline("kill_elder_guardian");
        if (this.getType() == EntityTypes.WARDEN && this.lastHurtByPlayer != null) timer.tryInsertNewTimeline("kill_warden");
        if (this.getType() == EntityTypes.ENDER_DRAGON) timer.tryInsertNewTimeline("kill_ender_dragon");

        //Kill All Bosses
        if (timer.getCategory() == RunCategories.KILL_ALL_BOSSES) {
            if (this.getType() == EntityTypes.ENDER_DRAGON) {
                timer.updateMoreData(0, 1);
            }
            if (this.getType() == EntityTypes.WITHER && this.lastHurtByPlayer != null) {
                timer.updateMoreData(1, 1);
                RunCategories.checkAllBossesCompleted();
            }
            if (this.getType() == EntityTypes.ELDER_GUARDIAN && this.lastHurtByPlayer != null) {
                timer.updateMoreData(2, 1);
                RunCategories.checkAllBossesCompleted();
            }
            if (this.getType() == EntityTypes.WARDEN && this.lastHurtByPlayer != null) {
                timer.updateMoreData(3, 1);
                RunCategories.checkAllBossesCompleted();
            }
        }

        //Kill Wither
        if (timer.getCategory() == RunCategories.KILL_WITHER && this.getType() == EntityTypes.WITHER && this.lastHurtByPlayer != null) {
            InGameTimer.complete();
        }

        //Kill Elder Guardian
        if (timer.getCategory() == RunCategories.KILL_ELDER_GUARDIAN && this.getType() == EntityTypes.ELDER_GUARDIAN && this.lastHurtByPlayer != null) {
            InGameTimer.complete();
        }

        //Kill Warden
        if (timer.getCategory() == RunCategories.KILL_WARDEN && this.getType() == EntityTypes.WARDEN && this.lastHurtByPlayer != null) {
            InGameTimer.complete();
        }

        if (this.getType() == EntityTypes.ENDER_DRAGON && !this.level().isClientSide()) {
            InGameTimerUtils.IS_KILLED_ENDER_DRAGON = true;
        }
    }
}
