package us.potatoboy.skywars.game.map.loot;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.game.map.SkyWarsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootHelper {
    public static void fillChests(ServerWorld world, SkyWarsMap map, int count) {
        Random random = new Random();

        fillChestType(world, map, count, random, map.spawnChests, "spawn");
        fillChestType(world, map, count, random, map.centerChests, "center");
    }

    private static void fillChestType(ServerWorld world, SkyWarsMap map, int count, Random random, List<BlockPos> chests, String type) {
        for (BlockPos pos : chests) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity != null && blockEntity instanceof LootableContainerBlockEntity) {
                LootableContainerBlockEntity lootable = (LootableContainerBlockEntity) blockEntity;
                lootable.clear();
                lootable.setLootTable(SkyWars.identifier( type + "/fill" + count), random.nextLong());
            }
        }
    }
}
