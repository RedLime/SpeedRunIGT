package com.redlimerl.speedrunigt.mixins.timeline;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BookshelfBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BookshelfBlock.class)
public abstract class BookshelfBlockMixin extends Block {
    protected BookshelfBlockMixin(Material material) {
        super(material);
    }

    @Override
    public void harvest(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity be) {
        if (pos.getY() < 55) {
            InGameTimer.getInstance().tryInsertNewTimeline("break_underground_bookshelf");
        }
        super.harvest(world, player, pos, state, be);
    }
}