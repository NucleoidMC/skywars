package us.potatoboy.skywars.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import xyz.nucleoid.plasmid.game.config.CombatConfig;
import us.potatoboy.skywars.game.map.SkyWarsMapConfig;

public class SkyWarsConfig {
    public static final Codec<SkyWarsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("dimension", DimensionType.OVERWORLD_ID).forGetter(config -> config.dimension),
            CombatConfig.CODEC.optionalFieldOf("combat", CombatConfig.DEFAULT).forGetter(config -> config.combat),
            SkyWarsMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            Codec.INT.fieldOf("refills").forGetter(config -> config.refills),
            Codec.INT.fieldOf("refill_mins").forGetter(config -> config.refill_mins),
            Codec.INT.fieldOf("time_limit_mins").forGetter(config -> config.timeLimitMins)
    ).apply(instance, SkyWarsConfig::new));

    public final SkyWarsMapConfig mapConfig;
    public final int timeLimitMins;
    public final Identifier dimension;
    public final CombatConfig combat;
    public int refills;
    public int refill_mins;

    public SkyWarsConfig( Identifier dimension, CombatConfig combat, SkyWarsMapConfig mapConfig, int refills, int refill_mins, int timeLimitMins) {
        this.mapConfig = mapConfig;
        this.timeLimitMins = timeLimitMins;
        this.dimension = dimension;
        this.combat = combat;
        this.refills = refills;
        this.refill_mins = refill_mins;
    }
}
