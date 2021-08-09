package us.potatoboy.skywars.game;

import com.google.common.collect.Multimap;
import eu.pb4.sidebars.api.Sidebar;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.game.map.SkyWarsMap;
import us.potatoboy.skywars.utility.FormattingUtil;
import us.potatoboy.skywars.utility.TextUtil;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.*;

public class SkyWarsActive {
    public final SkyWarsConfig config;

    public final GameSpace gameSpace;
    public final SkyWarsMap gameMap;
    public final ServerWorld world;
    public final GameActivity gameActivity;

    public final Object2ObjectMap<ServerPlayerEntity, SkyWarsPlayer> participants;
    public Multimap<Team, ServerPlayerEntity> teams;
    private Set<Team> allTeams;
    private final SkyWarsSpawnLogic spawnLogic;
    public final SkyWarsStageManager stageManager;
    protected final Sidebar globalSidebar = new Sidebar(Sidebar.Priority.MEDIUM);
    public final boolean ignoreWinState;

    private SkyWarsActive(GameSpace gameSpace, ServerWorld world, SkyWarsMap map, GlobalWidgets widgets, SkyWarsConfig config, Object2ObjectMap<ServerPlayerEntity, SkyWarsPlayer> participants, GameActivity gameLogic, Multimap<Team, ServerPlayerEntity> teams) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.gameMap = map;
        this.world = world;
        this.gameActivity = gameLogic;
        this.teams = teams;
        this.allTeams = new HashSet<>(teams.keySet());
        this.spawnLogic = new SkyWarsSpawnLogic(gameSpace, map);
        this.participants = participants;

        this.stageManager = new SkyWarsStageManager(this);
        this.ignoreWinState = this.teams.keySet().size() <= 1;

        this.buildSidebar();
        this.globalSidebar.show();

        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            var data = this.participants.get(player);

            if (data != null) {
                this.setPlayerSidebar(player, data);
            } else {
                this.globalSidebar.addPlayer(player);
            }
        }
    }

    public static void open(GameSpace gameSpace, ServerWorld world, SkyWarsMap map, SkyWarsConfig config, Multimap<Team, ServerPlayerEntity> teams, Object2ObjectMap<ServerPlayerEntity, SkyWarsPlayer> participants) {
        gameSpace.setActivity(activity -> {
            GlobalWidgets widgets = GlobalWidgets.addTo(activity);
            SkyWarsActive active = new SkyWarsActive(gameSpace, world, map, widgets, config, participants, activity, teams);

            activity.allow(GameRuleType.CRAFTING);
            activity.deny(GameRuleType.PORTALS);
            activity.allow(GameRuleType.PVP);
            activity.allow(GameRuleType.HUNGER);
            activity.allow(GameRuleType.FALL_DAMAGE);
            activity.deny(GameRuleType.INTERACTION);
            activity.allow(GameRuleType.BLOCK_DROPS);
            activity.allow(GameRuleType.THROW_ITEMS);
            activity.deny(GameRuleType.UNSTABLE_TNT);
            activity.allow(GameRuleType.PLAYER_PROJECTILE_KNOCKBACK);
            activity.allow(GameRuleType.TRIDENTS_LOYAL_IN_VOID);
            activity.allow(SkyWars.PROJECTILE_PLAYER_MOMENTUM);
            activity.allow(SkyWars.REDUCED_EXPLOSION_DAMAGE);

            activity.listen(GameActivityEvents.ENABLE, active::onOpen);
            activity.listen(GameActivityEvents.DISABLE, active::onClose);

            activity.listen(GamePlayerEvents.OFFER, offer -> offer.accept(world, active.spawnLogic.getRandomSpawnPos(offer.player().getRandom())));
            activity.listen(GamePlayerEvents.ADD, active::addPlayer);
            activity.listen(GamePlayerEvents.REMOVE, active::removePlayer);

            activity.listen(GameActivityEvents.TICK, active::tick);

            activity.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
            activity.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
            activity.listen(BlockPlaceEvent.BEFORE, active::onPlaceBlock);
        });
    }

    private void onOpen() {
        spawnParticipants();

        this.stageManager.onOpen(world.getTime(), this.config);
    }

    private SkyWarsPlayer getParticipant(ServerPlayerEntity player) {
        return participants.get(player);
    }

    private void spawnParticipants() {
        Collections.shuffle(gameMap.spawns);

        Iterator<Vec3d> spawnIterator = gameMap.spawns.listIterator();
        for (Team team : teams.keySet()) {
            Vec3d spawn = spawnIterator.next();
            for (ServerPlayerEntity player : teams.get(team)) {
                this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
                this.spawnLogic.spawnPlayer(player, spawn, world);
                player.getInventory().clear();
                player.closeHandledScreen();
            }
        }
    }

    private void onClose() {
        globalSidebar.hide();
        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            var data = this.participants.get(player);
            if (data != null) {
                data.sidebar.removePlayer(player);
            } else {
                this.globalSidebar.removePlayer(player);
            }
        }
    }

    private void addPlayer(ServerPlayerEntity player) {
        globalSidebar.addPlayer(player);
        spawnSpectator(player);
    }

    private void eliminatePlayer(ServerPlayerEntity player) {
        if (participants.containsKey(player)) {
            participants.remove(player);
            teams.values().remove(player);
        } else {
            globalSidebar.removePlayer(player);
        }
    }

    private void removePlayer(ServerPlayerEntity player) {
        eliminatePlayer(player);
        if (participants.containsKey(player)) {
            getParticipant(player).sidebar.removePlayer(player);
        }
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        SkyWarsPlayer participant = getParticipant(player);
        long time = world.getTime();

        if (participant != null && source.getAttacker() != null && source.getAttacker() instanceof ServerPlayerEntity) {
            PlayerRef attacker = PlayerRef.of((PlayerEntity) source.getAttacker());
            participant.lastTimeAttacked = new AttackRecord(attacker, time);
        }

        return ActionResult.PASS;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        gameSpace.getPlayers().sendMessage(getDeathMessage(player, source));

        player.getInventory().dropAll();
        this.spawnSpectator(player);

        eliminatePlayer(player);
        return ActionResult.FAIL;
    }

    private ActionResult onPlaceBlock(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state, ItemUsageContext context) {
        int slot;
        if (context.getHand() == Hand.MAIN_HAND) {
            slot = player.getInventory().selectedSlot;
        } else {
            slot = 40; // offhand
        }

        if (!gameMap.template.getBounds().contains(pos)) {
            player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, slot, context.getStack()));
            player.sendMessage(new TranslatableText("text.skywars.border").formatted(Formatting.RED, Formatting.BOLD), false);
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    private Text getDeathMessage(ServerPlayerEntity player, DamageSource source) {
        SkyWarsPlayer participant = getParticipant(player);
        long time = world.getTime();

        Text deathMessage = source.getDeathMessage(player);
        deathMessage = new LiteralText("☠ ")
                .styled(style -> Style.EMPTY.withColor(TextColor.fromRgb(0x858585)))
                .append(deathMessage)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xbfbfbf)));
        SkyWarsPlayer attacker = null;

        if (source.getAttacker() != null) {
            if (source.getAttacker() instanceof ServerPlayerEntity) {
                attacker = getParticipant((ServerPlayerEntity) source.getAttacker());
            }
        } else if (participant != null && participant.attacker(time, world) != null) {
            attacker = getParticipant(participant.attacker(time, world));
        }

        if (attacker != null) {
            attacker.kills += 1;
        }

        return deathMessage;
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        player.getInventory().clear();
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(player, world);
    }

    private void tick() {
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
        }
    }

    private void broadcastWin(WinResult result) {
        Team winningTeam = result.getWinningTeam();

        Text message = getWinMessage(winningTeam);

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.playSound(SoundEvents.ENTITY_VILLAGER_YES);
    }

    private Text getWinMessage(Team winningTeam) {
        if (winningTeam != null) {
            MutableText message = new LiteralText("");
            List<ServerPlayerEntity> winners = new ArrayList<>(teams.get(winningTeam));
            for (int i = 0; i < winners.size(); i++) {
                message = switch (i) {
                    case 0 -> new LiteralText("").append(winners.get(i).getDisplayName()).append(message);
                    case 1 -> new LiteralText("").append(winners.get(i).getDisplayName()).append(" & ").append(message);
                    default -> new LiteralText("").append(winners.get(i).getDisplayName()).append(", ").append(message);
                };
            }

            if (winners.size() <= 1) {
                return message.append(new TranslatableText("text.skywars.win")).formatted(Formatting.GOLD);
            } else {
                return message.append(new TranslatableText("text.skywars.win.team")).formatted(Formatting.GOLD);
            }
        } else {
            return new TranslatableText("text.skywars.win.zone").formatted(Formatting.GOLD);
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
                MobEntity entity = EntityType.WITHER.create(world);
                entity.setTarget(target);
                entities.add(entity);
                break;
            case 1:
                entity = EntityType.ENDER_DRAGON.create(world);
                ((EnderDragonEntity) entity).getPhaseManager().setPhase(PhaseType.CHARGING_PLAYER);
                ((EnderDragonEntity) entity).getPhaseManager().create(PhaseType.CHARGING_PLAYER).setPathTarget(new Vec3d(target.getX(), target.getY(), target.getZ()));
                entities.add(entity);
                break;
            case 2:
                for (int i = 0; i < 10; i++) {
                    entity = EntityType.BEE.create(world);
                    ((BeeEntity) entity).setAngerTime(1000000000);
                    entity.setTarget(target);
                    ((BeeEntity) entity).setAngryAt(target.getUuid());
                    entities.add(entity);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + eventID);
        }

        for (MobEntity entity : entities) {
            Vec3d pos = SkyWarsSpawnLogic.choosePos(entity.getRandom(), gameMap.getSpawn(entity.getRandom()), 2f);
            entity.refreshPositionAfterTeleport(pos);

            world.spawnEntity(entity);
        }
    }

    private void setPlayerSidebar(ServerPlayerEntity player, SkyWarsPlayer data) {
        data.sidebar = this.globalSidebar;
        this.globalSidebar.addPlayer(player);
    }

    private void buildSidebar() {
        this.globalSidebar.setTitle(TextUtil.getText("sidebar", "title").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withBold(true)));

        this.globalSidebar.set(b -> {

            b.add(LiteralText.EMPTY);

            b.add((player) ->
                    FormattingUtil.formatScoreboard(
                            FormattingUtil.GENERAL_PREFIX,
                            TextUtil.getText("sidebar", (config.teamSize() > 1 ? "teams" : "players"),
                                    new LiteralText(String.valueOf(teams.keySet().size())).formatted(Formatting.WHITE)
                            ).formatted(Formatting.GREEN)
                    )
            );

            b.add(LiteralText.EMPTY);

            b.add((player) -> {
                if (player != null) {
                    SkyWarsPlayer data = this.participants.get(player);

                    if (data != null) {
                        return FormattingUtil.formatScoreboard(
                                FormattingUtil.DEATH_PREFIX,
                                Style.EMPTY.withColor(Formatting.GOLD),
                                TextUtil.getText("sidebar", "kills",
                                        new LiteralText("" + data.kills).formatted(Formatting.WHITE)
                                )
                        );
                    }
                }
                return LiteralText.EMPTY;
            });

            b.add(LiteralText.EMPTY);

            b.add((player) -> {
                var time = Math.max(stageManager.refillTime - world.getTime(), 0);
                return FormattingUtil.formatScoreboard(
                        FormattingUtil.TIME_PREFIX,
                        Style.EMPTY.withColor(Formatting.GREEN),
                        TextUtil.getText("sidebar", "refill",
                                new LiteralText(formatTime(time)).formatted(Formatting.WHITE))
                );
            });
            b.add((player) -> {
                var time = Math.max(stageManager.finishTime - world.getTime(), 0);
                return FormattingUtil.formatScoreboard(
                        FormattingUtil.COMET_PREFIX,
                        Style.EMPTY.withColor(Formatting.GREEN),
                        TextUtil.getText("sidebar", "armageddon",
                                new LiteralText(formatTime(time)).formatted(Formatting.WHITE))
                );
            });
        });
    }

    public static String formatTime(long ticksUntilEnd) {
        long secondsUntilEnd = ticksUntilEnd / 20;

        long minutes = secondsUntilEnd / 60;
        long seconds = secondsUntilEnd % 60;

        return String.format("%02d:%02d", minutes, seconds);
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
