package us.potatoboy.skywars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import us.potatoboy.skywars.game.map.SkyWarsMap;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameSpace;

import java.util.Random;

public class SkyWarsSpawnLogic {
    private final GameSpace gameSpace;
    private final SkyWarsMap map;

    public SkyWarsSpawnLogic(GameSpace gameSpace, SkyWarsMap map) {
        this.gameSpace = gameSpace;
        this.map = map;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.changeGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
        player.getHungerManager().add(20, 2.0f);
        player.setHealth(20.0f);
    }

    public void spawnPlayer(ServerPlayerEntity player, ServerWorld world) {
        spawnPlayer(player, getRandomSpawnPos(player.getRandom()), world);
    }

    public void spawnPlayer(ServerPlayerEntity player, Vec3d pos, ServerWorld world) {
        player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
        player.setOnGround(true);
    }

    public Vec3d getRandomSpawnPos(Random random) {
        return choosePos(random, map.getSpawn(random), 0);
    }

    public static Vec3d choosePos(Random random, BlockBounds bounds, float aboveGround) {
        BlockPos min = bounds.min();
        BlockPos max = bounds.max();

        double x = MathHelper.nextDouble(random, min.getX(), max.getX());
        double z = MathHelper.nextDouble(random, min.getZ(), max.getZ());
        double y = min.getY() + aboveGround;

        return new Vec3d(x + 0.5, y, z + 0.5);
    }
}
