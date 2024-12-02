package us.potatoboy.skywars;

import eu.pb4.playerdata.api.PlayerDataApi;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.potatoboy.skywars.custom.SWBlocks;
import us.potatoboy.skywars.game.SkyWarsConfig;
import us.potatoboy.skywars.game.SkyWarsWaiting;
import us.potatoboy.skywars.kit.KitRegistry;
import us.potatoboy.skywars.kit.PlayerKitStorage;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;

import java.util.Random;

public class SkyWars implements ModInitializer {
    public static final String ID = "skywars";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final Random RANDOM = new Random();

    public static GameRuleType PROJECTILE_PLAYER_MOMENTUM = GameRuleType.create();
    public static GameRuleType REDUCED_EXPLOSION_DAMAGE = GameRuleType.create();

    public static final GameType<SkyWarsConfig> TYPE = GameType.register(
            Identifier.of(ID, "skywars"),
            SkyWarsConfig.CODEC,
            SkyWarsWaiting::open
    );

    @Override
    public void onInitialize() {
        SWBlocks.register();

        PlayerDataApi.register(PlayerKitStorage.STORAGE);
        KitRegistry.register();
    }

    public static Identifier identifier(String value) {
        return Identifier.of(ID, value);
    }
}
