package us.potatoboy.skywars.game.map;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.ArrayList;
import java.util.List;

public class SkyWarsMap {
    private final MapTemplate template;
    private final SkyWarsMapConfig config;
    public BlockBounds waitingSpawn;
    public List<BlockPos> spawns = new ArrayList<>();
    public List<BlockPos> spawnChests = new ArrayList<>();
    public List<BlockPos> centerChests = new ArrayList<>();

    public SkyWarsMap(MapTemplate template, SkyWarsMapConfig config) {
        this.template = template;
        this.config = config;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }
}
