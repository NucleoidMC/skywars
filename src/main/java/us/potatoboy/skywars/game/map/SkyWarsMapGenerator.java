package us.potatoboy.skywars.game.map;

import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.util.BlockBounds;
import net.minecraft.block.Blocks;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import us.potatoboy.skywars.game.SkyWarsConfig;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SkyWarsMapGenerator {
    private final SkyWarsMapConfig config;

    public SkyWarsMapGenerator(SkyWarsMapConfig config) {
        this.config = config;
    }

    public SkyWarsMap build() throws GameOpenException {
        try {
            MapTemplate template = MapTemplateSerializer.INSTANCE.loadFromResource(this.config.id);
            SkyWarsMap map = new SkyWarsMap(template, this.config);

            map.waitingSpawn = getRegion(template, "waiting_spawn");

            List<BlockPos> spawns = template.getMetadata().getRegions("spawn").map(region -> region.getBounds().getMin()).collect(Collectors.toList());
            if (spawns.size() == 0) {
                throw new GameOpenException(new LiteralText("No player spawns defined."));
            }

            map.spawns = spawns;

            map.spawnChests = template.getMetadata().getRegions("spawn_chest").map(region -> region.getBounds().getMin()).collect(Collectors.toList());
            map.centerChests = template.getMetadata().getRegions("center_chest").map(region -> region.getBounds().getMin()).collect(Collectors.toList());

            return map;
        } catch (IOException e) {
            throw new GameOpenException(new LiteralText("Failed to load map"));
        }
    }

    private static BlockBounds getRegion(MapTemplate template, String name) {
        BlockBounds bounds = template.getMetadata().getFirstRegionBounds(name);
        if (bounds == null) {
            throw new GameOpenException(new LiteralText(String.format("%s region not found", name)));
        }

        return bounds;
    }
}
