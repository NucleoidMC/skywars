package us.potatoboy.skywars.game.map.loot;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import us.potatoboy.skywars.game.SkyWarsConfig;
import us.potatoboy.skywars.game.map.SkyWarsMap;

import java.util.List;
import java.util.Random;

public class LootHelper {
    public static void fillChests(ServerLevel world, SkyWarsMap map, SkyWarsConfig config, int count) {
        Random random = new Random();

        fillChestType(world, count, random, map.spawnChests, config.spawnLootTable());
        fillChestType(world, count, random, map.centerChests, config.centerLootTable());
    }

    private static void fillChestType(ServerLevel world, int count, Random random, List<BlockPos> chests, Identifier id) {
        for (BlockPos pos : chests) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof RandomizableContainerBlockEntity lootable) {
                lootable.clearContent();
                lootable.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + count)), random.nextLong());
            }
        }
    }
}
