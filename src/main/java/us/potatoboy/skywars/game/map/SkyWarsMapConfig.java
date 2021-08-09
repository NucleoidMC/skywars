package us.potatoboy.skywars.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record SkyWarsMapConfig(Identifier id) {
    public static final Codec<SkyWarsMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(SkyWarsMapConfig::id)
    ).apply(instance, SkyWarsMapConfig::new));
}
