package us.potatoboy.skywars.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import org.apache.commons.lang3.RandomStringUtils;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.game.map.loot.LootHelper;
import us.potatoboy.skywars.kit.Kit;
import us.potatoboy.skywars.kit.KitRegistry;
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
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class SkyWarsWaiting {
    private final GameSpace gameSpace;
    private final SkyWarsMap map;
    public final SkyWarsConfig config;
    private final SkyWarsSpawnLogic spawnLogic;

    public final Object2ObjectMap<ServerPlayerEntity, SkyWarsPlayer> participants;

    private SkyWarsWaiting(GameSpace gameSpace, SkyWarsMap map, SkyWarsConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.spawnLogic = new SkyWarsSpawnLogic(gameSpace, map);
        this.participants = new Object2ObjectOpenHashMap<>();
    }

    public static GameOpenProcedure open(GameOpenContext<SkyWarsConfig> context) {
        SkyWarsConfig config = context.getConfig();
        SkyWarsMapGenerator generator = new SkyWarsMapGenerator(config.mapConfig);
        SkyWarsMap map = generator.build();

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(map.asGenerator(context.getServer()))
                .setDimensionType(RegistryKey.of(Registry.DIMENSION_TYPE_KEY, config.dimension))
                .setGameRule(GameRules.DO_FIRE_TICK, true)
                .setDefaultGameMode(GameMode.SPECTATOR);

        return context.createOpenProcedure(worldConfig, game -> {
            GameWaitingLobby.applyTo(game, new PlayerConfig(1, map.spawns.size() * config.teamSize, config.teamSize + 1, PlayerConfig.Countdown.DEFAULT));

            SkyWarsWaiting waiting = new SkyWarsWaiting(game.getSpace(), map, context.getConfig());

            game.on(RequestStartListener.EVENT, waiting::requestStart);
            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
            game.on(PlayerRemoveListener.EVENT, waiting::removePlayer);
            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
            game.on(UseItemListener.EVENT, waiting::onUseItem);

            LootHelper.fillChests(game.getSpace().getWorld(), map, config, 1);
        });
    }

    private TypedActionResult<ItemStack> onUseItem(ServerPlayerEntity playerEntity, Hand hand) {
        SkyWarsPlayer participant = participants.get(playerEntity);

        if (participant != null && playerEntity.inventory.getMainHandStack().getItem() == Items.COMPASS) {
            SkyWarsKitSelector.openSelector(playerEntity, participant, this);
        }

        return TypedActionResult.pass(playerEntity.getStackInHand(hand));
    }

    private void removePlayer(ServerPlayerEntity playerEntity) {
        participants.remove(playerEntity);
    }

    private StartResult requestStart() {
        ServerScoreboard scoreboard = gameSpace.getServer().getScoreboard();

        HashSet<Team> teams = new HashSet<>();
        List<Formatting> teamColors = new ArrayList<>(Arrays.asList(
                Formatting.BLUE,
                Formatting.RED,
                Formatting.YELLOW,
                Formatting.GREEN,
                Formatting.GOLD,
                Formatting.AQUA,
                Formatting.GOLD,
                Formatting.LIGHT_PURPLE,
                Formatting.DARK_PURPLE,
                Formatting.DARK_AQUA,
                Formatting.DARK_RED,
                Formatting.DARK_GREEN,
                Formatting.DARK_BLUE,
                Formatting.DARK_GRAY,
                Formatting.BLACK
        ));

        for (int i = 0; i < Math.round(gameSpace.getPlayers().size() / (float) config.teamSize); i++) {
            Team team = scoreboard.addTeam(RandomStringUtils.randomAlphabetic(16));
            team.setFriendlyFireAllowed(false);
            team.setShowFriendlyInvisibles(true);
            team.setCollisionRule(AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS);
            team.setDisplayName(new LiteralText("Team"));
            if (config.teamSize > 1) team.setColor(teamColors.get(i));

            teams.add(team);
        }

        TeamAllocator<Team, ServerPlayerEntity> allocator = new TeamAllocator<>(teams);

        for (ServerPlayerEntity playerEntity : gameSpace.getPlayers()) {
            allocator.add(playerEntity, null);
        }

        Multimap<Team, ServerPlayerEntity> teamPlayers = HashMultimap.create();
        allocator.allocate((team, player) -> {
            scoreboard.addPlayerToTeam(player.getEntityName(), team);
            teamPlayers.put(team, player);
        });

        SkyWarsActive.open(this.gameSpace, this.map, this.config, teamPlayers, participants);
        return StartResult.OK;
    }

    private void addPlayer(ServerPlayerEntity player) {
        SkyWarsPlayer participant = new SkyWarsPlayer();
        Kit selectedKit = KitRegistry.get(SkyWars.KIT_STORAGE.getPlayerKit(player.getUuid()));
        if (config.kits.left().isPresent()) {
            if (!config.kits.left().get().contains(selectedKit)) selectedKit = null;
        } else if (config.kits.right().isPresent()) {
            if (!config.kits.right().get()) selectedKit = null;
        }

        participant.selectedKit = selectedKit;
        participants.put(player, participant);
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

        player.inventory.setStack(0, ItemStackBuilder.of(Items.COMPASS)
                .setName(new TranslatableText("skywars.item.select_kit")
                        .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GOLD))).build());
    }
}