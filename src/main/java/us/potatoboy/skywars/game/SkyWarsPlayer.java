package us.potatoboy.skywars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class SkyWarsPlayer {
    //TODO kit
    public AttackRecord lastTimeAttacked;
    public int kills;

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
