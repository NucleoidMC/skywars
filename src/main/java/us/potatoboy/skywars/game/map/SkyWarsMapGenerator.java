package us.potatoboy.skywars.game.map;

import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;
import net.minecraft.block.Blocks;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import us.potatoboy.skywars.game.SkyWarsConfig;

import java.util.concurrent.CompletableFuture;

public class SkyWarsMapGenerator {

    private final SkyWarsMapConfig config;

    public SkyWarsMapGenerator(SkyWarsMapConfig config) {
        this.config = config;
    }

    public SkyWarsMap build() {
        MapTemplate template = MapTemplate.createEmpty();
        SkyWarsMap map = new SkyWarsMap(template, this.config);

        this.buildSpawn(template);
        map.spawn = new BlockPos(0,65,0);

        return map;
    }

    private void buildSpawn(MapTemplate builder) {
        BlockPos min = new BlockPos(-5, 64, -5);
        BlockPos max = new BlockPos(5, 64, 5);

        for (BlockPos pos : BlockPos.iterate(min, max)) {
            builder.setBlockState(pos, this.config.spawnBlock);
        }
    }
}
