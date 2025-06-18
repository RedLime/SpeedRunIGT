package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.mixins.access.ServerChunkProviderAccessor;
import com.redlimerl.speedrunigt.mixins.access.StructureFeatureAccessor;
import com.redlimerl.speedrunigt.mixins.access.SurfaceChunkGeneratorAccessor;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.NetherFortressStructure;
import net.minecraft.structure.StrongholdStructure;
import net.minecraft.structure.class_21;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.NetherChunkGenerator;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.dimension.TheNetherDimension;
import net.minecraft.world.gen.GeneratorConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin2 {

    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow public abstract BlockPos method_4086();

    @Inject(method = "tickPlayer", at = @At("TAIL"))
    public void onTick(CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE) {
            Block standingOn = this.getServerWorld().getBlock(this.method_4086().x, this.method_4086().y - 1, this.method_4086().z);
            if (this.getServerWorld().dimension instanceof TheNetherDimension) {
                NetherFortressStructure fortressFeature = ((NetherChunkGenerator) ((ServerChunkProviderAccessor) this.getServerWorld().getChunkProvider()).getChunkGenerator()).fortressFeature;
                boolean isInFortress = fortressFeature.method_46(this.method_4086().x, this.method_4086().y + 1, this.method_4086().z);
                boolean onNetherBrick = standingOn == Blocks.NETHER_BRICKS;
                if (isInFortress && onNetherBrick) {
                    timer.tryInsertNewTimeline("found_fortress");
                }
            }
            if (this.getServerWorld().dimension instanceof OverworldDimension) {
                StrongholdStructure strongholdStructure = ((SurfaceChunkGeneratorAccessor) ((ServerChunkProviderAccessor) this.getServerWorld().getChunkProvider()).getChunkGenerator()).getStrongholdGenerator();
                boolean isInStronghold = strongholdStructure.method_46(this.method_4086().x, this.method_4086().y + 1, this.method_4086().z);
                boolean inPortalRoom = false;
                if (isInStronghold) {
                    // method_46 already checks method_5516 != null so npe is not of concern
                    GeneratorConfig config = ((StructureFeatureAccessor) strongholdStructure).invokeMethod_5516(this.method_4086().x, this.method_4086().y + 1, this.method_4086().z);
                    for (Object piece : config.getChildren()) {
                        // class_21 is the portal room
                        if (piece instanceof class_21) {
                            inPortalRoom = (((class_21) piece).getBoundingBox().intersects(this.method_4086().x, this.method_4086().y + 1, this.method_4086().z));
                            break;
                        }
                    }
                }
                boolean onStoneBrick = standingOn == Blocks.STONE_BRICKS || standingOn == Blocks.MONSTER_EGG;
                if (inPortalRoom || isInStronghold && onStoneBrick) {
                    timer.tryInsertNewTimeline("found_stronghold");
                }
            }
        }
    }
}
