package us.potatoboy.skywars.game;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket.Flag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import us.potatoboy.skywars.game.map.loot.LootHelper;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;

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
        this.refillTime = startTime + ((long) config.refillMins() * 20 * 60);
        this.finishTime = this.startTime + ((long) config.timeLimitMins() * 20 * 60);
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
        if (game.checkWinResult().isWin()) {
            this.closeTime = time + (5 * 20);

            return IdleTickResult.GAME_FINISHED;
        }

        if (refills <= game.config.refills()) {
            if (time > refillTime) {
                refills++;
                LootHelper.fillChests(game.world, game.gameMap, game.config, refills);
                game.gameSpace.getPlayers().sendActionBar(Text.translatable("text.skywars.refill"), 5, 20, 5);
                game.gameSpace.getPlayers().playSound(SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 2.0F, 1.0F);

                if (refills <= game.config.refills()) this.refillTime = time + ((long) game.config.refillMins() * 20 * 60);
            }
        }

        if (time > this.finishTime) {
            //this.closeTime = time + (5 * 20);
            finishTime += 20 * 30;
            game.spawnGameEnd();
            game.gameSpace.getPlayers().showTitle(Text.translatable("text.skywars.armageddon").formatted(Formatting.BOLD, Formatting.RED), 5, 20, 5);
            game.gameSpace.getPlayers().playSound(SoundEvents.ENTITY_WITHER_SPAWN);
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
                player.networkHandler.requestTeleport(destX, destY, destZ, player.getYaw(), player.getPitch(), flags);
            }
        }

        int sec = (int) Math.floor(sec_f) - 1;

        if ((this.startTime - time) % 20 == 0) {
            PlayerSet players = space.getPlayers();

            if (sec > 0) {
                players.showTitle(Text.literal(Integer.toString(sec)).formatted(Formatting.BOLD), 20);
                players.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else {
                players.showTitle(Text.translatable("text.skywars.go").formatted(Formatting.BOLD), 10);
                players.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.PLAYERS, 1.0F, 2.0F);
                for (var playerRef : game.liveParticipants) {
                    var playerEntity = game.getPlayer(playerRef);
                    playerEntity.changeGameMode(GameMode.SURVIVAL);

                    SkyWarsPlayer participant = game.participants.get(playerRef);
                    if (participant.selectedKit != null) {
                        participant.selectedKit.equipPlayer(playerEntity);
                    }
                }
                game.gameActivity.allow(GameRuleType.INTERACTION);
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
