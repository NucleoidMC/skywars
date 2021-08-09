package us.potatoboy.skywars.game;

import eu.pb4.sidebars.api.Sidebar;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.kit.Kit;
import us.potatoboy.skywars.kit.KitRegistry;

public class SkyWarsPlayer {
    public AttackRecord lastTimeAttacked;
    public int kills = 0;
    public Kit selectedKit;
    public Team team = null;
    public Sidebar sidebar;

    public SkyWarsPlayer(ServerPlayerEntity player) {
        this.selectedKit = KitRegistry.get(SkyWars.KIT_STORAGE.getPlayerKit(player.getUuid()));
    }

    public ServerPlayerEntity attacker(long time, ServerWorld world) {
        if (lastTimeAttacked != null) {
            return lastTimeAttacked.isValid(time) ? lastTimeAttacked.player.getEntity(world) : null;
        }

        return null;
    }
}
