package us.potatoboy.skywars.game;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.GameMode;
import us.potatoboy.skywars.game.map.loot.LootHelper;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket.Flag;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class SkyWarsStageManager {
    private final SkyWarsActive game;

    public int refills = 1;

    private long closeTime = -1;
    public long finishTime = -1;
    private long startTime = -1;
    public long refillTime = -1;
    private final Object2ObjectMap<ServerPlayerEntity, FrozenPlayer> frozen;

    public SkyWarsStageManager(SkyWarsActive game) {
        this.game = game;
        this.frozen = new Object2ObjectOpenHashMap<>();
    }

    public void onOpen(long time, SkyWarsConfig config) {
        this.startTime = time - (time % 20) + (4 * 20) + 19;
        this.refillTime = startTime + (config.refill_mins * 20 * 60);
        this.finishTime = this.startTime + (config.timeLimitMins * 20 * 60);
    }

    public IdleTickResult tick(long time, GameSpace space) {
        if (space.getPlayers().isEmpty()) {
            return IdleTickResult.GAME_CLOSED;
        }

        // Game has finished. Wait a few seconds before finally closing the game.
        if (this.closeTime > 0) {
            if (time >= this.closeTime) {
                return IdleTickResult.GAME_CLOSED;
            }
            return IdleTickResult.TICK_FINISHED;
        }

        // Game hasn't started yet. Display a countdown before it begins.
        if (this.startTime > time) {
            this.tickStartWaiting(time, space);
            return IdleTickResult.TICK_FINISHED;
        }

        //Only one player remaining. Game finished
        int remainingCount = 0;
        for (ServerPlayerEntity player : space.getPlayers()) {
            if (!player.isSpectator()) {
                remainingCount++;
            }
        }
        if (remainingCount < 2 && !game.ignoreWinState) {
            this.closeTime = time + (5 * 20);

            return IdleTickResult.GAME_FINISHED;
        }

        if (refills <= game.config.refills) {
            if (time > refillTime) {
                refills++;
                LootHelper.fillChests(game.gameSpace.getWorld(), game.gameMap, refills);
                this.refillTime = time + (game.config.refill_mins * 20 * 60);
            }
        }

        // Game has just finished. Transition to the waiting-before-close state.
        if (time > this.finishTime) {
            this.closeTime = time + (5 * 20);

            return IdleTickResult.GAME_FINISHED;
        }

        return IdleTickResult.CONTINUE_TICK;
    }

    private void tickStartWaiting(long time, GameSpace space) {
        float sec_f = (this.startTime - time) / 20.0f;

        if (sec_f > 1) {
            for (ServerPlayerEntity player : space.getPlayers()) {
                if (player.isSpectator()) {
                    continue;
                }

                FrozenPlayer state = this.frozen.computeIfAbsent(player, p -> new FrozenPlayer());

                if (state.lastPos == null) {
                    state.lastPos = player.getPos();
                }

                double destX = state.lastPos.x;
                double destY = state.lastPos.y;
                double destZ = state.lastPos.z;

                // Set X and Y as relative so it will send 0 change when we pass yaw (yaw - yaw = 0) and pitch
                Set<Flag> flags = ImmutableSet.of(Flag.X_ROT, Flag.Y_ROT);

                // Teleport without changing the pitch and yaw
                player.networkHandler.teleportRequest(destX, destY, destZ, player.yaw, player.pitch, flags);
            }
        }

        int sec = (int) Math.floor(sec_f) - 1;

        if ((this.startTime - time) % 20 == 0) {
            PlayerSet players = space.getPlayers();

            if (sec > 0) {
                players.sendTitle(new LiteralText(Integer.toString(sec)).formatted(Formatting.BOLD));
                players.sendSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else {
                players.sendTitle(new LiteralText("Go!").formatted(Formatting.BOLD));
                players.sendSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.PLAYERS, 1.0F, 2.0F);
                for (ServerPlayerEntity playerEntity : players) {
                    playerEntity.setGameMode(GameMode.SURVIVAL);
                }
            }
        }
    }

    public static class FrozenPlayer {
        public Vec3d lastPos;
    }

    public enum IdleTickResult {
        CONTINUE_TICK,
        TICK_FINISHED,
        GAME_FINISHED,
        GAME_CLOSED,
    }
}
