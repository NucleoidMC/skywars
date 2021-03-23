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
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Random;

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
        player.getHungerManager().add(20, 2.0f);
        player.setHealth(20.0f);
    }

    public void spawnPlayer(ServerPlayerEntity player) {
        BlockBounds bounds = map.getSpawn(player.getRandom());
        if (bounds == null) {
            SkyWars.LOGGER.error("Cannot spawn player! No spawn is defined in the map!");
            return;
        }

        Vec3d pos = choosePos(player.getRandom(), bounds, 0);

        spawnPlayer(player, pos);
    }

    public void spawnPlayer(ServerPlayerEntity player, Vec3d pos) {
        ServerWorld world = this.gameSpace.getWorld();

        player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
        player.setOnGround(true);
    }

    public static Vec3d choosePos(Random random, BlockBounds bounds, float aboveGround) {
        BlockPos min = bounds.getMin();
        BlockPos max = bounds.getMax();

        double x = MathHelper.nextDouble(random, min.getX(), max.getX());
        double z = MathHelper.nextDouble(random, min.getZ(), max.getZ());
        double y = min.getY() + aboveGround;

        return new Vec3d(x + 0.5, y, z + 0.5);
    }
}
