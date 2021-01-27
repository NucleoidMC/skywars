package us.potatoboy.skywars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import us.potatoboy.skywars.kit.Kit;

public class SkyWarsPlayer {
    public AttackRecord lastTimeAttacked;
    public int kills;
    public Kit selectedKit;

    public SkyWarsPlayer() {
        kills = 0;
    }

    public ServerPlayerEntity attacker(long time, ServerWorld world) {
        if (lastTimeAttacked != null) {
            return lastTimeAttacked.isValid(time) ? lastTimeAttacked.player.getEntity(world) : null;
        }

        return null;
    }
}
