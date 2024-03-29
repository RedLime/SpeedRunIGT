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
        if (block.getLootTableId() == LootTables.EMPTY) return false;
        if (block == Blocks.NETHER_PORTAL) return false;
        if (block == Blocks.FARMLAND) return false;
        if (block == Blocks.GRASS_PATH) return false;
        if (block == Blocks.CHORUS_PLANT) return false;
        if (block == Blocks.LILY_PAD) return false;
        if (block == Blocks.PISTON_HEAD) return false;
        if (block == Blocks.TALL_SEAGRASS) return false;
        if (block == Blocks.NETHER_WART) return false;
        if (block == Blocks.SPAWNER) return false;
        if (block == Blocks.BUBBLE_COLUMN) return false;
        if (block == Blocks.BAMBOO_SAPLING) return false;
        if (block == Blocks.PETRIFIED_OAK_SLAB) return false;
        if (block == Blocks.FROSTED_ICE) return false;
        if (block == Blocks.PLAYER_HEAD) return false;
        if (block instanceof InfestedBlock) return false;

        if (block == Blocks.REDSTONE_WIRE) return false;
        if (block == Blocks.TRIPWIRE) return false;
        if (block == Blocks.SWEET_BERRY_BUSH) return false;
        if (block == Blocks.KELP) return false;
        if (block == Blocks.COCOA) return false;
        if (block instanceof FluidBlock) return false;
        if (block instanceof CropBlock) return false;
        if (block instanceof AttachedStemBlock) return false;
        if (block instanceof StemBlock) return false;

        return block != Blocks.AIR;
    }

    private static List<Item> getItemsForAllBlocks() {
        ArrayList<Item> items = Lists.newArrayList();

        items.add(Items.SWEET_BERRIES);
        items.add(Items.NETHER_WART);
        items.add(Items.KELP);
        items.add(Items.REDSTONE);
        items.add(Items.STRING);
        items.add(Items.WATER_BUCKET);
        items.add(Items.LAVA_BUCKET);
        items.add(Items.WHEAT_SEEDS);
        items.add(Items.BEETROOT_SEEDS);
        items.add(Items.POTATO);
        items.add(Items.CARROT);
        items.add(Items.MELON_SEEDS);
        items.add(Items.PUMPKIN_SEEDS);
        items.add(Items.COCOA_BEANS);

        return items;
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
