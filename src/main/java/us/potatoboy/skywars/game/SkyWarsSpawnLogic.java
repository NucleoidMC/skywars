package us.potatoboy.skywars.game;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.game.map.SkyWarsMap;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.Set;

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
        player.playerScreenHandler.setCursorStack(ItemStack.EMPTY);
        player.playerScreenHandler.getCraftingInput().clear();
        player.getInventory().clear();
    }

    public void spawnPlayer(ServerPlayerEntity player, ServerWorld world) {
        spawnPlayer(player, getRandomSpawnPos(), world);
    }

    public void spawnPlayer(ServerPlayerEntity player, Vec3d pos, ServerWorld world) {
        player.teleport(player.getServerWorld(), pos.getX(), pos.getY(), pos.getZ(), Set.of(), player.getYaw(), player.getPitch(), false);
        player.setOnGround(true);
    }

    public Vec3d getRandomSpawnPos() {
        return choosePos(map.getSpawn(), 0);
    }

    public static Vec3d choosePos(BlockBounds bounds, float aboveGround) {
        BlockPos min = bounds.min();
        BlockPos max = bounds.max();

        double x = SkyWars.RANDOM.nextDouble(min.getX(), max.getX()+1);
        double z = SkyWars.RANDOM.nextDouble(min.getZ(), max.getZ()+1);
        double y = min.getY() + aboveGround;

        return new Vec3d(x + 0.5, y, z + 0.5);
    }
}
