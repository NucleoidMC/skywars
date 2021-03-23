package us.potatoboy.skywars.game.map;

import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec3d;
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

            map.waitingSpawns = template.getMetadata().getRegionBounds("waiting_spawn").collect(Collectors.toList());

            List<Vec3d> spawns = template.getMetadata().getRegionBounds("spawn").map(bounds -> {
                Vec3d spawn = bounds.getCenter();
                spawn = spawn.subtract(0, (bounds.getMax().getY() - bounds.getMin().getY() + 1) / 2.0D, 0);
                return spawn;
            }).collect(Collectors.toList());
            if (spawns.size() == 0) {
                throw new GameOpenException(new LiteralText("No player spawns defined."));
            }

            map.spawns = spawns;

            map.spawnChests = template.getMetadata().getRegionBounds("spawn_chest").map(BlockBounds::getMin).collect(Collectors.toList());
            map.centerChests = template.getMetadata().getRegionBounds("center_chest").map(BlockBounds::getMin).collect(Collectors.toList());

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
