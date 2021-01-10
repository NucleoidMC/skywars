package us.potatoboy.skywars.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import us.potatoboy.skywars.game.map.SkyWarsMapConfig;

public class SkyWarsConfig {
    public static final Codec<SkyWarsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            SkyWarsMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs)
    ).apply(instance, SkyWarsConfig::new));

    public final PlayerConfig playerConfig;
    public final SkyWarsMapConfig mapConfig;
    public final int timeLimitSecs;

    public SkyWarsConfig(PlayerConfig players, SkyWarsMapConfig mapConfig, int timeLimitSecs) {
        this.playerConfig = players;
        this.mapConfig = mapConfig;
        this.timeLimitSecs = timeLimitSecs;
    }
}
