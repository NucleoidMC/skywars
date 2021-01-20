package us.potatoboy.skywars.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.apache.commons.lang3.RandomStringUtils;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.game.map.loot.LootHelper;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.event.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import us.potatoboy.skywars.game.map.SkyWarsMap;
import us.potatoboy.skywars.game.map.SkyWarsMapGenerator;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.TeamAllocator;

import java.util.HashSet;

public class SkyWarsWaiting {
    private final GameSpace gameSpace;
    private final SkyWarsMap map;
    private final SkyWarsConfig config;
    private final SkyWarsSpawnLogic spawnLogic;

    private SkyWarsWaiting(GameSpace gameSpace, SkyWarsMap map, SkyWarsConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.spawnLogic = new SkyWarsSpawnLogic(gameSpace, map);
    }

    public static GameOpenProcedure open(GameOpenContext<SkyWarsConfig> context) {
        SkyWarsConfig config = context.getConfig();
        SkyWarsMapGenerator generator = new SkyWarsMapGenerator(config.mapConfig);
        SkyWarsMap map = generator.build();

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(map.asGenerator(context.getServer()))
                .setDimensionType(RegistryKey.of(Registry.DIMENSION_TYPE_KEY, config.dimension))
                .setDefaultGameMode(GameMode.SPECTATOR);

        return context.createOpenProcedure(worldConfig, game -> {
            GameWaitingLobby.applyTo(game, new PlayerConfig(1, map.spawns.size(), config.teamSize + 1, PlayerConfig.Countdown.DEFAULT));

            SkyWarsWaiting waiting = new SkyWarsWaiting(game.getSpace(), map, context.getConfig());

            game.on(RequestStartListener.EVENT, waiting::requestStart);
            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);

            LootHelper.fillChests(game.getSpace().getWorld(), map, config, 1);
        });
    }

    private StartResult requestStart() {
        ServerScoreboard scoreboard = gameSpace.getServer().getScoreboard();

        HashSet<Team> teams = new HashSet<>();

        for (int i = 0; i < Math.round(gameSpace.getPlayers().size() / (float) config.teamSize); i++) {
            Team team = scoreboard.addTeam(RandomStringUtils.randomAlphabetic(16));
            team.setFriendlyFireAllowed(false);
            team.setShowFriendlyInvisibles(true);
            team.setCollisionRule(AbstractTeam.CollisionRule.NEVER);

            teams.add(team);
        }

        TeamAllocator allocator = new TeamAllocator(teams);

        for (ServerPlayerEntity playerEntity : gameSpace.getPlayers()) {
            allocator.add(playerEntity, null);
        }

        Multimap<Team, ServerPlayerEntity> teamPlayers = HashMultimap.create();
        allocator.allocate((team, player) -> {
            scoreboard.addPlayerToTeam(((ServerPlayerEntity)player).getEntityName(), (Team) team);
            teamPlayers.put((Team)team, (ServerPlayerEntity)player);
        });

        SkyWarsActive.open(this.gameSpace, this.map, this.config, teamPlayers);
        return StartResult.OK;
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
        return ActionResult.FAIL;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }
}