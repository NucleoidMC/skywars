package us.potatoboy.skywars.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import us.potatoboy.skywars.SkyWars;
import xyz.nucleoid.plasmid.game.config.CombatConfig;
import us.potatoboy.skywars.game.map.SkyWarsMapConfig;

public class SkyWarsConfig {
    public static final Codec<SkyWarsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("dimension", DimensionType.OVERWORLD_ID).forGetter(config -> config.dimension),
            SkyWarsMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            Identifier.CODEC.optionalFieldOf("spawn_loot_table", new Identifier(SkyWars.ID, "spawn/default")).forGetter(config -> config.spawnLootTable),
            Identifier.CODEC.optionalFieldOf("center_loot_table", new Identifier(SkyWars.ID, "center/default")).forGetter(config -> config.centerLootTable),
            Codec.INT.fieldOf("refills").forGetter(config -> config.refills),
            Codec.INT.fieldOf("refill_mins").forGetter(config -> config.refill_mins),
            Codec.INT.fieldOf("time_limit_mins").forGetter(config -> config.timeLimitMins)
    ).apply(instance, SkyWarsConfig::new));

    public final SkyWarsMapConfig mapConfig;
    public final int timeLimitMins;
    public final Identifier dimension;
    public Identifier spawnLootTable;
    public Identifier centerLootTable;
    public int refills;
    public int refill_mins;

    public SkyWarsConfig( Identifier dimension, SkyWarsMapConfig mapConfig, Identifier spawnLootTable, Identifier centerLootTable, int refills, int refill_mins, int timeLimitMins) {
        this.mapConfig = mapConfig;
        this.timeLimitMins = timeLimitMins;
        this.dimension = dimension;
        this.spawnLootTable = spawnLootTable;
        this.centerLootTable = centerLootTable;
        this.refills = refills;
        this.refill_mins = refill_mins;
    }
}
