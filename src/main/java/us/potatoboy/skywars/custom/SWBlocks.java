package us.potatoboy.skywars.custom;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.custom.block.LaunchPadBlock;
import us.potatoboy.skywars.custom.block.LaunchPadBlockEntity;

public class SWBlocks {
    public static final Block GOLD_LAUNCH_PAD = new LaunchPadBlock(AbstractBlock.Settings.copy(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE).strength(100).noCollision().registryKey(RegistryKey.of(RegistryKeys.BLOCK, SkyWars.identifier("gold_launch_pad"))), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.getDefaultState());
    public static final Block IRON_LAUNCH_PAD = new LaunchPadBlock(AbstractBlock.Settings.copy(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE).strength(100).noCollision().registryKey(RegistryKey.of(RegistryKeys.BLOCK, SkyWars.identifier("iron_launch_pad"))), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState());

    public static final BlockEntityType<LaunchPadBlockEntity> LAUNCH_PAD_ENTITY = FabricBlockEntityTypeBuilder.create(LaunchPadBlockEntity::new, GOLD_LAUNCH_PAD, IRON_LAUNCH_PAD).build(null);

    public static void register() {
        register("gold_launch_pad", GOLD_LAUNCH_PAD);
        register("iron_launch_pad", IRON_LAUNCH_PAD);

        registerBlockEntity("launch_pad", LAUNCH_PAD_ENTITY);
    }

    private static <T extends Block> T register(String id, T block) {
        return Registry.register(Registries.BLOCK, SkyWars.identifier(id), block);
    }

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String id, BlockEntityType<T> type) {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, SkyWars.identifier(id), type);
        PolymerBlockUtils.registerBlockEntity(LAUNCH_PAD_ENTITY);
        return type;
    }
}
