package us.potatoboy.skywars.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.api.game.GameOpenException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SkyWarsMapGenerator {
    private final SkyWarsMapConfig config;

    public SkyWarsMapGenerator(SkyWarsMapConfig config) {
        this.config = config;
    }

    public SkyWarsMap build(MinecraftServer server) throws GameOpenException {
        try {
            var template = MapTemplateSerializer.loadFromResource(server, this.config.id());
            SkyWarsMap map = new SkyWarsMap(template, this.config);

            map.waitingSpawns = template.getMetadata().getRegionBounds("waiting_spawn").collect(Collectors.toList());

            List<Vec3d> spawns = template.getMetadata().getRegionBounds("spawn").map(bounds -> {
                Vec3d spawn = bounds.center();
                spawn = spawn.subtract(0, (bounds.max().getY() - bounds.min().getY() + 1) / 2.0D, 0);
                return spawn;
            }).collect(Collectors.toList());
            if (spawns.isEmpty()) {
                throw new GameOpenException(Text.literal("No player spawns defined."));
            }

            map.spawns = spawns;

            map.spawnChests = template.getMetadata().getRegionBounds("spawn_chest").map(BlockBounds::min).collect(Collectors.toList());
            map.centerChests = template.getMetadata().getRegionBounds("center_chest").map(BlockBounds::min).collect(Collectors.toList());

            return map;
        } catch (IOException e) {
            throw new GameOpenException(Text.literal("Failed to load map"));
        }
    }

    private static BlockBounds getRegion(MapTemplate template, String name) {
        BlockBounds bounds = template.getMetadata().getFirstRegionBounds(name);
        if (bounds == null) {
            throw new GameOpenException(Text.literal(String.format("%s region not found", name)));
        }

        return bounds;
    }
}
