package us.potatoboy.skywars.game;

import com.google.common.base.Predicates;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import us.potatoboy.skywars.SkyWars;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameLogic;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;
import xyz.nucleoid.plasmid.util.PlayerRef;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import us.potatoboy.skywars.game.map.SkyWarsMap;

import java.util.*;
import java.util.stream.Collectors;

public class SkyWarsActive {
    public final SkyWarsConfig config;

    public final GameSpace gameSpace;
    public final SkyWarsMap gameMap;
    public final GameLogic gameLogic;

    public final Object2ObjectMap<ServerPlayerEntity, SkyWarsPlayer> participants;
    public Multimap<Team, ServerPlayerEntity> teams;
    private Set<Team> allTeams;
    private final SkyWarsSpawnLogic spawnLogic;
    public final SkyWarsStageManager stageManager;
    private final SkyWarsSidebar sidebar;
    public final boolean ignoreWinState;

    private SkyWarsActive(GameSpace gameSpace, SkyWarsMap map, GlobalWidgets widgets, SkyWarsConfig config, Object2ObjectMap<ServerPlayerEntity, SkyWarsPlayer> participants, GameLogic gameLogic, Multimap<Team, ServerPlayerEntity> teams) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.gameMap = map;
        this.gameLogic = gameLogic;
        this.teams = teams;
        this.allTeams = new HashSet<>(teams.keySet());
        this.spawnLogic = new SkyWarsSpawnLogic(gameSpace, map);
        this.participants = participants;

        this.sidebar = new SkyWarsSidebar(this);
        this.stageManager = new SkyWarsStageManager(this);
        this.ignoreWinState = this.teams.keySet().size() <= 1;
    }

    public static void open(GameSpace gameSpace, SkyWarsMap map, SkyWarsConfig config, Multimap<Team, ServerPlayerEntity> teams, Object2ObjectMap<ServerPlayerEntity, SkyWarsPlayer> participants) {
        gameSpace.openGame(game -> {
            GlobalWidgets widgets = new GlobalWidgets(game);
            SkyWarsActive active = new SkyWarsActive(gameSpace, map, widgets, config, participants, game, teams);

            game.setRule(GameRule.CRAFTING, RuleResult.ALLOW);
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.ALLOW);
            game.setRule(GameRule.HUNGER, RuleResult.ALLOW);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.ALLOW);
            game.setRule(GameRule.INTERACTION, RuleResult.DENY);
            game.setRule(GameRule.BLOCK_DROPS, RuleResult.ALLOW);
            game.setRule(GameRule.THROW_ITEMS, RuleResult.ALLOW);
            game.setRule(GameRule.UNSTABLE_TNT, RuleResult.DENY);
            game.setRule(GameRule.PLAYER_PROJECTILE_KNOCKBACK, RuleResult.ALLOW);
            game.setRule(GameRule.TRIDENTS_LOYAL_IN_VOID, RuleResult.ALLOW);
            game.setRule(SkyWars.PROJECTILE_PLAYER_MOMENTUM, RuleResult.ALLOW);
            game.setRule(SkyWars.REDUCED_EXPLOSION_DAMAGE, RuleResult.ALLOW);

            game.on(GameOpenListener.EVENT, active::onOpen);
            game.on(GameCloseListener.EVENT, active::onClose);

            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
            game.on(PlayerAddListener.EVENT, active::addPlayer);
            game.on(PlayerRemoveListener.EVENT, active::removePlayer);

            game.on(GameTickListener.EVENT, active::tick);

            game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
            game.on(PlaceBlockListener.EVENT, active::onPlaceBlock);
        });
    }

    private void onOpen() {
        ServerWorld world = this.gameSpace.getWorld();
        spawnParticipants();

        this.stageManager.onOpen(world.getTime(), this.config);
    }

    private SkyWarsPlayer getParticipant(ServerPlayerEntity player) {
        return participants.get(player);
    }

    private void spawnParticipants() {
        ServerWorld world = this.gameSpace.getWorld();
        Collections.shuffle(gameMap.spawns);

        Iterator<BlockPos> spawnIterator = gameMap.spawns.listIterator();
        for (Team team : teams.keySet()) {
            BlockPos spawn = spawnIterator.next();
            for (ServerPlayerEntity player : teams.get(team)) {
                this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
                this.spawnLogic.spawnPlayer(player, spawn);
                player.inventory.clear();
                player.closeHandledScreen();
            }
        }
    }

    private void onClose() {
        sidebar.close();
        allTeams.forEach(team -> gameSpace.getServer().getScoreboard().removeTeam(team));
    }

    private void addPlayer(ServerPlayerEntity player) {
        spawnSpectator(player);
    }

    private void removePlayer(ServerPlayerEntity player) {
        if (participants.containsKey(player)) {
            sidebar.sidebars.get(getParticipant(player)).removePlayer(player);
            participants.remove(player);
            teams.values().remove(player);
            teams.containsValue(player);
        }
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        SkyWarsPlayer participant = getParticipant(player);
        long time = gameSpace.getWorld().getTime();

        if (participant != null && source.getAttacker() != null && source.getAttacker() instanceof ServerPlayerEntity) {
            PlayerRef attacker = PlayerRef.of((PlayerEntity) source.getAttacker());
            participant.lastTimeAttacked = new AttackRecord(attacker, time);
        }

        return ActionResult.PASS;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        MutableText deathMessage = getDeathMessage(player, source);
        gameSpace.getPlayers().sendMessage(deathMessage.formatted(Formatting.GRAY));

        player.inventory.dropAll();
        this.spawnSpectator(player);

        removePlayer(player);
        return ActionResult.FAIL;
    }

    private ActionResult onPlaceBlock(ServerPlayerEntity playerEntity, BlockPos pos, BlockState blockState, ItemUsageContext context) {
        int slot;
        if (context.getHand() == Hand.MAIN_HAND) {
            slot = playerEntity.inventory.selectedSlot;
        } else {
            slot = 40; // offhand
        }

        if(!gameMap.template.getBounds().contains(pos)) {
            playerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, slot, context.getStack()));
            playerEntity.sendMessage(new LiteralText("Border reached").formatted(Formatting.RED, Formatting.BOLD), false);
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    private MutableText getDeathMessage(ServerPlayerEntity player, DamageSource source) {
        SkyWarsPlayer participant = getParticipant(player);
        ServerWorld world = gameSpace.getWorld();
        long time = world.getTime();

        MutableText deathMessage = new LiteralText(" was killed by ");
        SkyWarsPlayer attacker = null;

        if (source.getAttacker() != null) {
            deathMessage.append(source.getAttacker().getDisplayName());

            if (source.getAttacker() instanceof ServerPlayerEntity) {
                attacker = getParticipant((ServerPlayerEntity) source.getAttacker());
            }
        } else if (participant != null && participant.attacker(time, world) != null) {
            deathMessage.append(participant.attacker(time, world).getDisplayName());
            attacker = getParticipant(participant.attacker(time, world));
        } else {
            deathMessage = new LiteralText(" died");
        }

        if (attacker != null) {
            attacker.kills += 1;
        }

        return new LiteralText("").append(player.getDisplayName()).append(deathMessage);
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        player.inventory.clear();
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(player);
    }

    private void tick() {
        ServerWorld world = this.gameSpace.getWorld();
        long time = world.getTime();

        SkyWarsStageManager.IdleTickResult result = this.stageManager.tick(time, gameSpace);

        switch (result) {
            case CONTINUE_TICK:
                break;
            case TICK_FINISHED:
                return;
            case GAME_FINISHED:
                this.broadcastWin(this.checkWinResult());
                return;
            case GAME_CLOSED:
                this.gameSpace.close(GameCloseReason.FINISHED);
                return;
        }

        if (time % 20 == 0) {
            sidebar.update(time);
        }
    }

    private void broadcastWin(WinResult result) {
        Team winningTeam = result.getWinningTeam();

        Text message = getWinMessage(winningTeam);

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.sendSound(SoundEvents.ENTITY_VILLAGER_YES);
    }

    private Text getWinMessage(Team winningTeam) {
        if (winningTeam != null) {
            MutableText message = new LiteralText("");
            //message = new LiteralText(" has won the game!").formatted(Formatting.GOLD);
            List<ServerPlayerEntity> winners = new ArrayList<>(teams.get(winningTeam));
            for (int i = 0; i < winners.size(); i++) {
                switch (i) {
                    case 0:
                        message = new LiteralText("").append(winners.get(i).getDisplayName()).append(message);
                        break;
                    case 1:
                        message = new LiteralText("").append(winners.get(i).getDisplayName()).append(" and ").append(message);
                        break;
                    default:
                        message = new LiteralText("").append(winners.get(i).getDisplayName()).append(", ").append(message);
                        break;
                }
            }

            if (winners.size() <= 1) {
                return message.append(" has won the game!").formatted(Formatting.GOLD);
            } else {
                return message.append(" have won the game!").formatted(Formatting.GOLD);
            }
        } else {
            return new LiteralText("The game ended, but nobody won!").formatted(Formatting.GOLD);
        }
    }

    public WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            if (teams.keySet().size() == 0) {
                return WinResult.win(null);
            } else {
                return WinResult.no();
            }
        }

        if (teams.keySet().size() == 1) {
            return WinResult.win((Team) teams.keySet().toArray()[0]);
        }

        return WinResult.no();
    }

    public void spawnGameEnd() {
        Random random = new Random();
        int eventID = random.nextInt(3);
        List<MobEntity> entities = new ArrayList<>();
        ServerPlayerEntity target = (ServerPlayerEntity) participants.keySet().toArray()[participants.size() == 1 ? 0 : random.nextInt(participants.size())];

        switch (eventID) {
            case 0:
                MobEntity entity = EntityType.WITHER.create(gameSpace.getWorld());
                entity.setTarget(target);
                entities.add(entity);
                break;
            case 1:
                entity = EntityType.ENDER_DRAGON.create(gameSpace.getWorld());
                ((EnderDragonEntity) entity).getPhaseManager().setPhase(PhaseType.CHARGING_PLAYER);
                ((EnderDragonEntity) entity).getPhaseManager().create(PhaseType.CHARGING_PLAYER).setTarget(new Vec3d(target.getX(), target.getY(), target.getZ()));
                entities.add(entity);
                break;
            case 2:
                for (int i = 0; i < 10; i++) {
                    entity = EntityType.BEE.create(gameSpace.getWorld());
                    ((BeeEntity)entity).setAngerTime(1000000000);
                    ((BeeEntity)entity).setTarget(target);
                    ((BeeEntity)entity).setAngryAt(target.getUuid());
                    entities.add(entity);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + eventID);
        }

        for (MobEntity entity : entities) {
            Vec3d pos = SkyWarsSpawnLogic.choosePos(new Random(), gameMap.waitingSpawn, 2f);
            entity.refreshPositionAfterTeleport(pos);

            gameSpace.getWorld().spawnEntity(entity);
        }
    }

    static class WinResult {
        final Team winningTeam;
        final boolean win;

        private WinResult(Team winningTeam, boolean win) {
            this.winningTeam = winningTeam;
            this.win = win;
        }

        static WinResult no() {
            return new WinResult(null, false);
        }

        static WinResult win(Team team) {
            return new WinResult(team, true);
        }

        public boolean isWin() {
            return this.win;
        }

        public Team getWinningTeam() {
            return this.winningTeam;
        }
    }
}
