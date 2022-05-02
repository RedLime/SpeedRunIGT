package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.class_3460;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
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

    @Shadow protected PlayerEntity attackingPlayer;

    public LivingEntityMixin(class_3460<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "onKilled")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        @NotNull InGameTimer timer = InGameTimer.getInstance();

        if (this.removed || this.dead || timer.getStatus() == TimerStatus.NONE) return;

        // For Timelines
        if (timer.getCategory() == RunCategories.KILL_ALL_BOSSES) {
            if (this.method_15557() == class_3460.field_16737 && this.attackingPlayer != null) timer.tryInsertNewTimeline("kill_wither");
            if (this.method_15557() == class_3460.field_16798 && this.attackingPlayer != null) timer.tryInsertNewTimeline("kill_elder_guardian");
            if (this.method_15557() == class_3460.field_16800) timer.tryInsertNewTimeline("kill_ender_dragon");
        }

        //Kill All Bosses
        if (timer.getCategory() == RunCategories.KILL_ALL_BOSSES) {
            if (this.method_15557() == class_3460.field_16800) {
                timer.updateMoreData(0, 1);
            }
            if (this.method_15557() == class_3460.field_16737) {
                timer.updateMoreData(1, 1);
                RunCategories.checkAllBossesCompleted();
            }
            if (this.method_15557() == class_3460.field_16798) {
                timer.updateMoreData(2, 1);
                RunCategories.checkAllBossesCompleted();
            }
        }

        //Kill Wither
        if (timer.getCategory() == RunCategories.KILL_WITHER && this.method_15557() == class_3460.field_16737) {
            InGameTimer.complete();
        }

        //Kill Elder Guardian
        if (timer.getCategory() == RunCategories.KILL_ELDER_GUARDIAN && this.method_15557() == class_3460.field_16798) {
            InGameTimer.complete();
        }

        if (this.method_15557() == class_3460.field_16800) {
            InGameTimerUtils.IS_KILLED_ENDER_DRAGON = true;
        }
    }
}
