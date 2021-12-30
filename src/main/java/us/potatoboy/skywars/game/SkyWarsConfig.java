package us.potatoboy.skywars.game;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.game.map.SkyWarsMapConfig;
import xyz.nucleoid.fantasy.Fantasy;

import java.util.List;

public record SkyWarsConfig(
        SkyWarsMapConfig mapConfig, int timeLimitMins, Identifier dimension, Identifier spawnLootTable,
        Identifier centerLootTable, int refills, int refillMins, int teamSize, Either<List<Identifier>, Boolean> kits
) {
    public static final Codec<SkyWarsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SkyWarsMapConfig.CODEC.fieldOf("map").forGetter(SkyWarsConfig::mapConfig),
            Codec.INT.fieldOf("time_limit_mins").forGetter(SkyWarsConfig::timeLimitMins),
            Identifier.CODEC.optionalFieldOf("dimension", Fantasy.DEFAULT_DIM_TYPE.getValue()).forGetter(SkyWarsConfig::dimension),
            Identifier.CODEC.optionalFieldOf("spawn_loot_table", new Identifier(SkyWars.ID, "spawn/default")).forGetter(SkyWarsConfig::spawnLootTable),
            Identifier.CODEC.optionalFieldOf("center_loot_table", new Identifier(SkyWars.ID, "center/default")).forGetter(SkyWarsConfig::centerLootTable),
            Codec.INT.fieldOf("refills").forGetter(SkyWarsConfig::refills),
            Codec.INT.fieldOf("refill_mins").forGetter(SkyWarsConfig::refillMins),
            Codec.INT.optionalFieldOf("team_size", 1).forGetter(SkyWarsConfig::teamSize),
            Codec.either(Codec.list(Identifier.CODEC), Codec.BOOL).fieldOf("kits").forGetter(SkyWarsConfig::kits)
    ).apply(instance, SkyWarsConfig::new));
}
