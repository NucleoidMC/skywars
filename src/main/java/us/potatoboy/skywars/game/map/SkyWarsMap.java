package us.potatoboy.skywars.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import us.potatoboy.skywars.SkyWars;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;

import java.util.ArrayList;
import java.util.List;

public class SkyWarsMap {
    public final MapTemplate template;
    private final SkyWarsMapConfig config;
    public List<BlockBounds> waitingSpawns;
    public List<Vec3d> spawns = new ArrayList<>();
    public List<BlockPos> spawnChests = new ArrayList<>();
    public List<BlockPos> centerChests = new ArrayList<>();

    public SkyWarsMap(MapTemplate template, SkyWarsMapConfig config) {
        this.template = template;
        this.config = config;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }

    public BlockBounds getSpawn() {
        return waitingSpawns.get(SkyWars.RANDOM.nextInt(waitingSpawns.size()));
    }
}
