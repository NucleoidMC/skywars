package us.potatoboy.skywars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public class AttackRecord {
    public static final long EXPIRE_TIME = 20 * 10;

    public final PlayerRef player;
    private final long expireTime;

    public AttackRecord(PlayerRef player, long time) {
        this.player = player;
        this.expireTime = time + EXPIRE_TIME;
    }

    public static AttackRecord fromAttacker(ServerPlayerEntity player) {
        return new AttackRecord(PlayerRef.of(player), player.getWorld().getTime());
    }

    public boolean isValid(long time) {
        return time < this.expireTime;
    }
}
