package us.potatoboy.skywars.game;

import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.GameSpace;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.game.map.SkyWarsMap;

public class SkyWarsSpawnLogic {
    private final GameSpace gameSpace;
    private final SkyWarsMap map;

    public SkyWarsSpawnLogic(GameSpace gameSpace, SkyWarsMap map) {
        this.gameSpace = gameSpace;
        this.map = map;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.setGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));
    }

    public void spawnPlayer(ServerPlayerEntity player) {
        ServerWorld world = this.gameSpace.getWorld();

        BlockPos pos = this.map.spawn;
        if (pos == null) {
            SkyWars.LOGGER.error("Cannot spawn player! No spawn is defined in the map!");
            return;
        }

        float radius = 4.5f;
        float x = pos.getX() + MathHelper.nextFloat(player.getRandom(), -radius, radius);
        float z = pos.getZ() + MathHelper.nextFloat(player.getRandom(), -radius, radius);

        player.teleport(world, x, pos.getY(), z, 0.0F, 0.0F);
    }
}
