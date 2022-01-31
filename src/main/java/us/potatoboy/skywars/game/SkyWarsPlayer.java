package us.potatoboy.skywars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.kit.Kit;
import us.potatoboy.skywars.kit.KitRegistry;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

public class SkyWarsPlayer {
    public int kills = 0;
    public Kit selectedKit;
    public GameTeam team = null;

    public SkyWarsPlayer(ServerPlayerEntity player) {
        this.selectedKit = KitRegistry.get(SkyWars.KIT_STORAGE.getPlayerKit(player.getUuid()));
    }
}
