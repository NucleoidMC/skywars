package us.potatoboy.skywars.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import org.apache.commons.lang3.RandomStringUtils;
import us.potatoboy.skywars.game.map.SkyWarsMap;
import us.potatoboy.skywars.game.map.SkyWarsMapGenerator;
import us.potatoboy.skywars.game.map.loot.LootHelper;
import us.potatoboy.skywars.game.ui.KitSelectorUI;
import us.potatoboy.skywars.kit.Kit;
import us.potatoboy.skywars.kit.KitRegistry;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.common.team.TeamAllocator;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class SkyWarsWaiting {
    private final GameSpace gameSpace;
    private final ServerWorld world;
    private final SkyWarsMap map;
    public final SkyWarsConfig config;
    public final ArrayList<Kit> kits = new ArrayList<>();
    private final SkyWarsSpawnLogic spawnLogic;

    public final Object2ObjectMap<ServerPlayerEntity, SkyWarsPlayer> participants;

    private SkyWarsWaiting(GameSpace gameSpace, ServerWorld world, SkyWarsMap map, SkyWarsConfig config) {
        this.gameSpace = gameSpace;
        this.world = world;
        this.map = map;
        this.config = config;
        this.spawnLogic = new SkyWarsSpawnLogic(gameSpace, map);
        this.participants = new Object2ObjectOpenHashMap<>();

        config.kits().ifLeft(kits -> {
            this.kits.addAll(kits.stream().map(KitRegistry::get).collect(Collectors.toList()));
        });

        config.kits().ifRight(allKits -> {
            if (allKits) {
                this.kits.addAll(KitRegistry.getKITS().values());
            }
        });
    }

    public static GameOpenProcedure open(GameOpenContext<SkyWarsConfig> context) {
        var config = context.config();
        var generator = new SkyWarsMapGenerator(config.mapConfig());
        var map = generator.build(context.server());

        var worldConfig = new RuntimeWorldConfig()
                .setGenerator(map.asGenerator(context.server()))
                .setDimensionType(RegistryKey.of(Registry.DIMENSION_TYPE_KEY, config.dimension()))
                .setGameRule(GameRules.DO_FIRE_TICK, true);

        return context.openWithWorld(worldConfig, (game, world) -> {
            GameWaitingLobby.addTo(game, new PlayerConfig(1, map.spawns.size() * config.teamSize(), config.teamSize() + 1, PlayerConfig.Countdown.DEFAULT));

            var waiting = new SkyWarsWaiting(game.getGameSpace(), world, map, context.config());

            game.listen(GamePlayerEvents.OFFER, offer -> offer.accept(world, waiting.spawnLogic.getRandomSpawnPos(offer.player().getRandom())));
            game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
            game.listen(GamePlayerEvents.JOIN, waiting::playerJoin);
            game.listen(GamePlayerEvents.LEAVE, waiting::playerLeave);
            game.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
            game.listen(ItemUseEvent.EVENT, waiting::onUseItem);

            LootHelper.fillChests(world, map, config, 1);
        });
    }

    private TypedActionResult<ItemStack> onUseItem(ServerPlayerEntity playerEntity, Hand hand) {
        SkyWarsPlayer participant = participants.get(playerEntity);

        if (participant != null && playerEntity.getInventory().getMainHandStack().getItem() == Items.COMPASS) {
            KitSelectorUI.openSelector(playerEntity, this);
        }

        return TypedActionResult.pass(playerEntity.getStackInHand(hand));
    }

    private void playerLeave(ServerPlayerEntity playerEntity) {
        participants.remove(playerEntity);
    }

    private GameResult requestStart() {
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

        for (int i = 0; i < Math.round(gameSpace.getPlayers().size() / (float) config.teamSize()); i++) {
            Team team = scoreboard.addTeam(RandomStringUtils.randomAlphabetic(16));
            team.setFriendlyFireAllowed(false);
            team.setShowFriendlyInvisibles(true);
            team.setCollisionRule(AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS);
            team.setDisplayName(new LiteralText("Team"));
            if (config.teamSize() > 1) team.setColor(teamColors.get(i));

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

        SkyWarsActive.open(this.gameSpace, world, this.map, this.config, teamPlayers, participants);
        return GameResult.ok();
    }

    private void playerJoin(ServerPlayerEntity player) {
        SkyWarsPlayer participant = new SkyWarsPlayer(player);
        if (config.kits().left().isPresent()) {
            if (!config.kits().left().get().contains(KitRegistry.getId(participant.selectedKit))) participant.selectedKit = null;
        } else if (config.kits().right().isPresent()) {
            if (!config.kits().right().get()) participant.selectedKit = null;
        }

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
        this.spawnLogic.spawnPlayer(player, world);

        player.getInventory().setStack(0, ItemStackBuilder.of(Items.COMPASS)
                .setName(new TranslatableText("skywars.item.select_kit")
                        .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GOLD))).build());
    }
}