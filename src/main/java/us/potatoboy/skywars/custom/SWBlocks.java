package us.potatoboy.skywars.custom;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.custom.block.LaunchPadBlock;
import us.potatoboy.skywars.custom.block.LaunchPadBlockEntity;

public class SWBlocks {
    public static final Block GOLD_LAUNCH_PAD = new LaunchPadBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE).strength(100).noCollision().setId(ResourceKey.create(Registries.BLOCK, SkyWars.identifier("gold_launch_pad"))), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultBlockState());
    public static final Block IRON_LAUNCH_PAD = new LaunchPadBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE).strength(100).noCollision().setId(ResourceKey.create(Registries.BLOCK, SkyWars.identifier("iron_launch_pad"))), Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultBlockState());

    public static final BlockEntityType<LaunchPadBlockEntity> LAUNCH_PAD_ENTITY = FabricBlockEntityTypeBuilder.create(LaunchPadBlockEntity::new, GOLD_LAUNCH_PAD, IRON_LAUNCH_PAD).build(null);

    public static void register() {
        register("gold_launch_pad", GOLD_LAUNCH_PAD);
        register("iron_launch_pad", IRON_LAUNCH_PAD);

        registerBlockEntity("launch_pad", LAUNCH_PAD_ENTITY);
    }

    private static <T extends Block> T register(String id, T block) {
        return Registry.register(BuiltInRegistries.BLOCK, SkyWars.identifier(id), block);
    }

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String id, BlockEntityType<T> type) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, SkyWars.identifier(id), type);
        PolymerBlockUtils.registerBlockEntity(LAUNCH_PAD_ENTITY);
        return type;
    }
}
