package us.potatoboy.skywars.game;

import eu.pb4.sidebars.api.Sidebar;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
