package us.potatoboy.skywars;

import net.fabricmc.api.ModInitializer;
import us.potatoboy.skywars.kit.KitRegistry;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.potatoboy.skywars.game.SkyWarsConfig;
import us.potatoboy.skywars.game.SkyWarsWaiting;
import xyz.nucleoid.plasmid.game.rule.GameRule;

public class SkyWars implements ModInitializer {
    public static final String ID = "skywars";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static GameRule PROJECTILE_PLAYER_MOMENTUM = new GameRule();

    public static final GameType<SkyWarsConfig> TYPE = GameType.register(
            new Identifier(ID, "skywars"),
            SkyWarsWaiting::open,
            SkyWarsConfig.CODEC
    );

    @Override
    public void onInitialize() {
        KitRegistry.register();
    }

    public static Identifier identifier(String value) {
        return new Identifier(ID, value);
    }
}
