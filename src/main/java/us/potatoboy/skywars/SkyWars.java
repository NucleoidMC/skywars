package us.potatoboy.skywars;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.potatoboy.skywars.game.SkyWarsConfig;
import us.potatoboy.skywars.game.SkyWarsWaiting;
import us.potatoboy.skywars.kit.KitRegistry;
import us.potatoboy.skywars.kit.PlayerKitStorage;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.storage.ServerStorage;

public class SkyWars implements ModInitializer {
    public static final String ID = "skywars";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final PlayerKitStorage KIT_STORAGE = ServerStorage.createStorage(identifier("kits"), new PlayerKitStorage());

    public static GameRuleType PROJECTILE_PLAYER_MOMENTUM = GameRuleType.create();
    public static GameRuleType REDUCED_EXPLOSION_DAMAGE = GameRuleType.create();

    public static final GameType<SkyWarsConfig> TYPE = GameType.register(
            new Identifier(ID, "skywars"),
            SkyWarsConfig.CODEC,
            SkyWarsWaiting::open
    );

    @Override
    public void onInitialize() {
        KitRegistry.register();
    }

    public static Identifier identifier(String value) {
        return new Identifier(ID, value);
    }
}
