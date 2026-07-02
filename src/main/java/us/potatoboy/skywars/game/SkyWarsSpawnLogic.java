package us.potatoboy.skywars.game;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
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

    public void resetPlayer(ServerPlayer player, GameType gameMode) {
        player.setGameMode(gameMode);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0f;
        player.getFoodData().eat(20, 2.0f);
        player.setHealth(20.0f);
        player.inventoryMenu.setCarried(ItemStack.EMPTY);
        player.inventoryMenu.getCraftSlots().clearContent();
        player.getInventory().clearContent();
    }

    public void spawnPlayer(ServerPlayer player, ServerLevel level) {
        spawnPlayer(player, getRandomSpawnPos(), level);
    }

    public void spawnPlayer(ServerPlayer player, Vec3 pos, ServerLevel level) {
        player.teleportTo(player.level(), pos.x(), pos.y(), pos.z(), Set.of(), player.getYRot(), player.getXRot(), false);
        player.setOnGround(true);
    }

    public Vec3 getRandomSpawnPos() {
        return choosePos(map.getSpawn(), 0);
    }

    public static Vec3 choosePos(BlockBounds bounds, float aboveGround) {
        BlockPos min = bounds.min();
        BlockPos max = bounds.max();

        double x = SkyWars.RANDOM.nextDouble(min.getX(), max.getX()+1);
        double z = SkyWars.RANDOM.nextDouble(min.getZ(), max.getZ()+1);
        double y = min.getY() + aboveGround;

        return new Vec3(x + 0.5, y, z + 0.5);
    }
}
