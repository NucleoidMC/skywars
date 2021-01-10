package us.potatoboy.skywars;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.potatoboy.skywars.game.SkyWarsConfig;
import us.potatoboy.skywars.game.SkyWarsWaiting;

public class SkyWars implements ModInitializer {

    public static final String ID = "skywars";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<SkyWarsConfig> TYPE = GameType.register(
            new Identifier(ID, "skywars"),
            SkyWarsWaiting::open,
            SkyWarsConfig.CODEC
    );

    @Override
    public void onInitialize() {}
}
