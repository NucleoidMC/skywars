package us.potatoboy.skywars.game;

import net.minecraft.server.level.ServerPlayer;
import us.potatoboy.skywars.kit.Kit;
import us.potatoboy.skywars.kit.KitRegistry;
import us.potatoboy.skywars.kit.PlayerKitStorage;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;

public class SkyWarsPlayer {
    public int kills = 0;
    public Kit selectedKit;
    public GameTeam team = null;

    public SkyWarsPlayer(ServerPlayer player) {
        this.selectedKit = KitRegistry.get(PlayerKitStorage.get(player).selectedKit);
    }
}
