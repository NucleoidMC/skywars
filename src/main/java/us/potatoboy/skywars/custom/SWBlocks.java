package us.potatoboy.skywars.custom;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.custom.block.LaunchPadBlock;
import us.potatoboy.skywars.custom.block.LaunchPadBlockEntity;

public class SWBlocks {
    public static final Block GOLD_LAUNCH_PAD = new LaunchPadBlock(AbstractBlock.Settings.of(Material.STONE).strength(100).noCollision(), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
    public static final Block IRON_LAUNCH_PAD = new LaunchPadBlock(AbstractBlock.Settings.of(Material.STONE).strength(100).noCollision(), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);

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
