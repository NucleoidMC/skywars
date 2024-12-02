package us.potatoboy.skywars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import us.potatoboy.skywars.kit.Kit;
import us.potatoboy.skywars.kit.KitRegistry;
import us.potatoboy.skywars.kit.PlayerKitStorage;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;

public class SkyWarsPlayer {
    public int kills = 0;
    public Kit selectedKit;
    public GameTeam team = null;

    public SkyWarsPlayer(ServerPlayerEntity player) {
        this.selectedKit = KitRegistry.get(PlayerKitStorage.get(player).selectedKit);
    }
}
