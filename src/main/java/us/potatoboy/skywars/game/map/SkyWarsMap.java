package us.potatoboy.skywars.game.map;

import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class SkyWarsMap {
    private final MapTemplate template;
    private final SkyWarsMapConfig config;
    public BlockPos spawn;

    public SkyWarsMap(MapTemplate template, SkyWarsMapConfig config) {
        this.template = template;
        this.config = config;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }
}
