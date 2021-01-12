package us.potatoboy.skywars.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class SkyWarsMapConfig {
    public static final Codec<SkyWarsMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(config -> config.id)
    ).apply(instance, SkyWarsMapConfig::new));

    public Identifier id;

    public SkyWarsMapConfig(Identifier id) {
        this.id = id;
    }
}
