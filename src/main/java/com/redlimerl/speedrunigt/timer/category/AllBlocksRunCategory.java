package com.redlimerl.speedrunigt.timer.category;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AllBlocksRunCategory extends RunCategory {
    public AllBlocksRunCategory() {
        super("all_blocks", "", "All Blocks");
    }

    @Override
    public String getLeaderboardUrl() {
        return "https://docs.google.com/spreadsheets/d/1RnN6lE3yi5S_5PBuxMXdWNvN3HayP3054M3Qud_p9BU/";
    }

    public boolean isCompleted(MinecraftServer server) {
        Set<String> placedBlocks = Sets.newHashSet();

        for (Item item : getAllItems()) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getStatHandler().getStat(Stats.USED, item) > 0) {
                    placedBlocks.add(Registry.ITEM.getId(item).toString());
                    break;
                }
            }
        }

        return placedBlocks.size() >= 650;
    }

    private static boolean isIncludedAllBlocks(Block block) {
        List<Block> excludeBlocks = Arrays.asList(
                Blocks.NETHER_PORTAL,
                Blocks.FARMLAND,
                Blocks.GRASS_PATH,
                Blocks.CHORUS_PLANT,
                Blocks.LILY_PAD,
                Blocks.PISTON_HEAD,
                Blocks.TALL_SEAGRASS,
                Blocks.NETHER_WART,
                Blocks.SPAWNER,
                Blocks.BUBBLE_COLUMN,
                Blocks.BAMBOO_SAPLING,
                Blocks.PETRIFIED_OAK_SLAB,
                Blocks.FROSTED_ICE,
                Blocks.PLAYER_HEAD,
                Blocks.REDSTONE_WIRE,
                Blocks.TRIPWIRE,
                Blocks.SWEET_BERRY_BUSH,
                Blocks.KELP,
                Blocks.COCOA,
                Blocks.AIR
        );

        if (block.getLootTableId() == LootTables.EMPTY) return false;
        if (block instanceof InfestedBlock) return false;
        if (block instanceof FluidBlock) return false;
        if (block instanceof CropBlock) return false;
        if (block instanceof AttachedStemBlock) return false;
        if (block instanceof StemBlock) return false;

        return !excludeBlocks.contains(block);
    }

    private static List<Item> getItemsForAllBlocks() {
        return Arrays.asList(
                Items.SWEET_BERRIES,
                Items.NETHER_WART,
                Items.KELP,
                Items.REDSTONE,
                Items.STRING,
                Items.WATER_BUCKET,
                Items.LAVA_BUCKET,
                Items.WHEAT_SEEDS,
                Items.BEETROOT_SEEDS,
                Items.POTATO,
                Items.CARROT,
                Items.MELON_SEEDS,
                Items.PUMPKIN_SEEDS,
                Items.COCOA_BEANS
        );
    }

    public static List<Item> getAllItems() {
        ArrayList<Item> items = Lists.newArrayList();

        for (Item item : Registry.ITEM) {
            if (getItemsForAllBlocks().contains(item)) items.add(item);

            if (item instanceof BlockItem) {
                BlockItem blockItem = (BlockItem) item;

                if (isIncludedAllBlocks(blockItem.getBlock())) items.add(item);
            }
        }

        return items;
    }
}
