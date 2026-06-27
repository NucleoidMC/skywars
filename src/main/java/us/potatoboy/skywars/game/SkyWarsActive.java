package us.potatoboy.skywars.game;

import com.google.common.collect.Multimap;
import eu.pb4.sidebars.api.Sidebar;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import us.potatoboy.skywars.SkyWars;
import us.potatoboy.skywars.SkywarsStatistics;
import us.potatoboy.skywars.game.map.SkyWarsMap;
import us.potatoboy.skywars.utility.FormattingUtil;
import us.potatoboy.skywars.utility.ComponentUtil;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.game.stats.GameStatisticBundle;
import xyz.nucleoid.plasmid.api.game.stats.StatisticKeys;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.FluidPlaceEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;
import xyz.nucleoid.stimuli.event.projectile.ArrowFireEvent;
import xyz.nucleoid.stimuli.event.projectile.ProjectileHitEvent;
import xyz.nucleoid.stimuli.event.world.FluidFlowEvent;

import java.util.*;

public class SkyWarsActive {
    public final SkyWarsConfig config;

    public final GameSpace gameSpace;
    public final SkyWarsMap gameMap;
    public final ServerLevel level;
    public final GameActivity gameActivity;

    public final Object2ObjectMap<PlayerRef, SkyWarsPlayer> participants;
    public final Set<PlayerRef> liveParticipants;
    public Multimap<GameTeam, PlayerRef> liveTeams;
    private final SkyWarsSpawnLogic spawnLogic;
    public final SkyWarsStageManager stageManager;
    public final GameStatisticBundle statistics;
    protected final Sidebar globalSidebar = new Sidebar(Sidebar.Priority.MEDIUM);
    public final boolean ignoreWinState;

    private SkyWarsActive(GameSpace gameSpace, ServerLevel level, SkyWarsMap map, GlobalWidgets widgets, SkyWarsConfig config, Object2ObjectMap<PlayerRef, SkyWarsPlayer> participants, GameActivity gameLogic, Multimap<GameTeam, PlayerRef> teams) {
        this.gameSpace = gameSpace;
        this.config = config;
        this.gameMap = map;
        this.level = level;
        this.gameActivity = gameLogic;
        this.liveTeams = teams;
        this.spawnLogic = new SkyWarsSpawnLogic(gameSpace, map);
        this.participants = new Object2ObjectOpenHashMap<>(participants);
        this.liveParticipants = participants.keySet();
        this.statistics = gameSpace.getStatistics().bundle(SkyWars.ID);

        this.stageManager = new SkyWarsStageManager(this);
        this.ignoreWinState = this.liveTeams.keySet().size() <= 1;

        this.buildSidebar();
        this.globalSidebar.show();

        for (ServerPlayer player : this.gameSpace.getPlayers()) {
            this.globalSidebar.addPlayer(player);
        }
    }

    public static void open(GameSpace gameSpace, ServerLevel level, SkyWarsMap map, SkyWarsConfig config, Multimap<GameTeam, PlayerRef> teams, Object2ObjectMap<PlayerRef, SkyWarsPlayer> participants) {
        gameSpace.setActivity(activity -> {
            GlobalWidgets widgets = GlobalWidgets.addTo(activity);
            SkyWarsActive active = new SkyWarsActive(gameSpace, level, map, widgets, config, participants, activity, teams);
            var teamManger = TeamManager.addTo(activity);
            for (GameTeam team : teams.keySet()) {
                teamManger.addTeam(team);
                for (PlayerRef playerRef : teams.get(team)) {
                    teamManger.addPlayerTo(playerRef, team.key());
                }
            }

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

            activity.listen(GamePlayerEvents.ACCEPT, offer -> offer.teleport(level, active.spawnLogic.getRandomSpawnPos()));
            activity.listen(GamePlayerEvents.ADD, active::addPlayer);
            activity.listen(GamePlayerEvents.REMOVE, active::removePlayer);

            activity.listen(GameActivityEvents.TICK, active::tick);

            activity.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
            activity.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
            activity.listen(BlockPlaceEvent.BEFORE, active::onPlaceBlock);
            activity.listen(ArrowFireEvent.EVENT, active::onArrowFire);
            activity.listen(ProjectileHitEvent.ENTITY, active::onProjectiveHit);
            activity.listen(FluidFlowEvent.EVENT, active::onFluidFlow);
            activity.listen(FluidPlaceEvent.EVENT, active::onFluidPlace);
        });
    }

    private void onOpen() {
        spawnParticipants();

        this.stageManager.onOpen(level.getGameTime(), this.config);
    }

    public SkyWarsPlayer getParticipant(PlayerRef player) {
        return participants.get(player);
    }

    public ServerPlayer getPlayer(PlayerRef ref) {
        return ref.getEntity(level);
    }

    private void spawnParticipants() {
        Collections.shuffle(gameMap.spawns);

        Iterator<Vec3> spawnIterator = gameMap.spawns.listIterator();
        for (GameTeam team : liveTeams.keySet()) {
            Vec3 spawn = spawnIterator.next();
            for (PlayerRef ref : liveTeams.get(team)) {
                this.statistics.forPlayer(ref).increment(StatisticKeys.GAMES_PLAYED, 1);

                var player = getPlayer(ref);
                if (player.containerMenu != player.inventoryMenu) player.closeContainer();
                this.spawnLogic.resetPlayer(player, GameType.ADVENTURE);
                this.spawnLogic.spawnPlayer(player, spawn, level);
            }
        }
    }

    private void onClose() {
        globalSidebar.hide();
        for (ServerPlayer player : this.gameSpace.getPlayers()) {
            this.globalSidebar.removePlayer(player);
        }
    }

    private void addPlayer(ServerPlayer player) {
        globalSidebar.addPlayer(player);
        spawnSpectator(player);
    }

    private void eliminatePlayer(PlayerRef player) {
        if (liveParticipants.contains(player)) {
            liveParticipants.remove(player);
            liveTeams.values().remove(player);
        }
    }

    private void removePlayer(ServerPlayer player) {
        eliminatePlayer(PlayerRef.of(player));
        globalSidebar.removePlayer(player);
    }

    private EventResult onPlayerDamage(ServerPlayer player, DamageSource source, float amount) {
        SkyWarsPlayer participant = getParticipant(PlayerRef.of(player));

        if (participant != null && source.getEntity() != null && source.getEntity() instanceof ServerPlayer attacker) {
            this.statistics.forPlayer(attacker).increment(StatisticKeys.DAMAGE_DEALT, amount);
            this.statistics.forPlayer(player).increment(StatisticKeys.DAMAGE_TAKEN, amount);
        }

        return EventResult.PASS;
    }

    private EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
        if (!liveParticipants.contains(PlayerRef.of(player))) return EventResult.DENY;

        gameSpace.getPlayers().sendMessage(getDeathMessage(player, source));

        player.getInventory().dropAll();
        this.spawnSpectator(player);

        eliminatePlayer(PlayerRef.of(player));
        return EventResult.DENY;
    }

    private EventResult onFluidFlow(ServerLevel serverLevel, BlockPos pos, BlockState blockState, Direction direction, BlockPos pos1, BlockState blockState1) {
        if (!gameMap.template.getBounds().contains(pos1)) {
            return EventResult.DENY;
        }

        return EventResult.PASS;
    }

    private EventResult onFluidPlace(ServerLevel serverLevel, BlockPos pos, @Nullable ServerPlayer player, @Nullable BlockHitResult blockHitResult) {
        if (!gameMap.template.getBounds().contains(pos)) {
            if (player != null) player.sendSystemMessage(Component.translatable("text.skywars.border").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
            return EventResult.DENY;
        }

        return EventResult.PASS;
    }


    private EventResult onPlaceBlock(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state, UseOnContext context) {
        int slot;
        if (context.getHand() == InteractionHand.MAIN_HAND) {
            slot = player.getInventory().getSelectedSlot();
        } else {
            slot = 40; // offhand
        }

        if (!gameMap.template.getBounds().contains(pos)) {
            player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, slot, context.getItemInHand()));
            player.sendSystemMessage(Component.translatable("text.skywars.border").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
            return EventResult.DENY;
        }

        return EventResult.PASS;
    }

    private EventResult onProjectiveHit(Projectile projectileEntity, EntityHitResult entityHitResult) {
        if (projectileEntity instanceof Arrow && projectileEntity.getOwner() instanceof ServerPlayer shooter) {
            this.statistics.forPlayer(shooter).increment(SkywarsStatistics.ARROWS_HIT, 1);
        }

        return EventResult.PASS;
    }

    private EventResult onArrowFire(ServerPlayer player, ItemStack itemStack, ArrowItem arrowItem, int i, AbstractArrow persistentProjectileEntity) {
        this.statistics.forPlayer(player).increment(SkywarsStatistics.ARROWS_SHOT, 1);

        return EventResult.PASS;
    }

    private Component getDeathMessage(ServerPlayer player, DamageSource source) {
        Component deathMessage = source.getLocalizedDeathMessage(player);
        deathMessage = Component.literal("☠ ")
                .withStyle(style -> Style.EMPTY.withColor(TextColor.fromRgb(0x858585)))
                .append(deathMessage)
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xbfbfbf)));
        ServerPlayer attacker = null;

        if (source.getEntity() != null) {
            if (source.getEntity() instanceof ServerPlayer adversary) {
                attacker = adversary;
            }
        } else if (player.getKillCredit() != null && player.getKillCredit() instanceof ServerPlayer adversary) {
            attacker = adversary;
        }

        if (attacker != null) {
            getParticipant(PlayerRef.of(attacker)).kills += 1;
            this.statistics.forPlayer(attacker).increment(StatisticKeys.KILLS, 1);
        }

        this.statistics.forPlayer(player).increment(StatisticKeys.DEATHS, 1);

        return deathMessage;
    }

    private void spawnSpectator(ServerPlayer player) {
        this.spawnLogic.resetPlayer(player, GameType.SPECTATOR);
        this.spawnLogic.spawnPlayer(player, level);
    }

    private void tick() {
        long time = level.getGameTime();

        if (time % 20 == 0) {
            for (ServerPlayer player : gameSpace.getPlayers()) {
                var ref = PlayerRef.of(player);
                if (player.getY() < gameMap.template.getBounds().min().getY() - 50) {
                    if(liveParticipants.contains(ref)) player.hurtServer(level, player.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                    else {
                        spawnLogic.resetPlayer(player, GameType.SPECTATOR);
                        spawnLogic.spawnPlayer(player, level);
                    }
                }
            }
        }

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
        GameTeam winningTeam = result.getWinningTeam();
        for (var player : participants.keySet()) {
            var team = getParticipant(player).team;
            if (team == winningTeam) {
                this.statistics.forPlayer(player).increment(StatisticKeys.GAMES_WON, 1);
            } else {
                this.statistics.forPlayer(player).increment(StatisticKeys.GAMES_LOST, 1);
            }
        }

        Component message = getWinMessage(winningTeam);

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.playSound(SoundEvents.VILLAGER_YES);
    }

    private Component getWinMessage(GameTeam winningTeam) {
        if (winningTeam != null) {
            MutableComponent message = Component.literal("");
            var winners = liveTeams.get(winningTeam).stream().map(this::getPlayer).toList();
            for (int i = 0; i < winners.size(); i++) {
                message = switch (i) {
                    case 0 -> Component.literal("").append(winners.get(i).getDisplayName()).append(message);
                    case 1 -> Component.literal("").append(winners.get(i).getDisplayName()).append(" & ").append(message);
                    default -> Component.literal("").append(winners.get(i).getDisplayName()).append(", ").append(message);
                };
            }

            if (winners.size() <= 1) {
                return message.append(Component.translatable("text.skywars.win")).withStyle(ChatFormatting.GOLD);
            } else {
                return message.append(Component.translatable("text.skywars.win.team")).withStyle(ChatFormatting.GOLD);
            }
        } else {
            return Component.translatable("text.skywars.win.none").withStyle(ChatFormatting.GOLD);
        }
    }

    public WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            if (liveTeams.keySet().isEmpty()) {
                return WinResult.win(null);
            } else {
                return WinResult.no();
            }
        }

        if (liveTeams.keySet().size() == 1) {
            return WinResult.win((GameTeam) liveTeams.keySet().toArray()[0]);
        }

        return WinResult.no();
    }

    public void spawnGameEnd() {
        Random random = new Random();
        int eventID = random.nextInt(3);
        List<Mob> entities = new ArrayList<>();
        ServerPlayer target = getPlayer((PlayerRef) liveParticipants.toArray()[liveParticipants.size() == 1 ? 0 : random.nextInt(liveParticipants.size())]);

        switch (eventID) {
            case 0:
                Mob entity = EntityTypes.WITHER.create(level, EntitySpawnReason.TRIGGERED);
                entity.setTarget(target);
                entities.add(entity);
                break;
            case 1:
                entity = EntityTypes.ENDER_DRAGON.create(level, EntitySpawnReason.TRIGGERED);
                ((EnderDragon) entity).getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                ((EnderDragon) entity).getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(new Vec3(target.getX(), target.getY(), target.getZ()));
                entities.add(entity);
                break;
            case 2:
                for (int i = 0; i < 10; i++) {
                    entity = EntityTypes.BEE.create(level, EntitySpawnReason.TRIGGERED);
                    ((Bee) entity).setPersistentAngerEndTime(level.getGameTime() + 1000000000);
                    entity.setTarget(target);
                    ((Bee) entity).setPersistentAngerTarget(EntityReference.of(target.getUUID()));
                    entities.add(entity);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + eventID);
        }

        for (Mob entity : entities) {
            Vec3 pos = SkyWarsSpawnLogic.choosePos(gameMap.getSpawn(), 2f);
            entity.snapTo(pos);

            level.addFreshEntity(entity);
        }
    }

    private void buildSidebar() {
        this.globalSidebar.setTitle(ComponentUtil.getComponent("sidebar", "title").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true)));

        this.globalSidebar.set(b -> {

            b.add(CommonComponents.EMPTY);

            b.add((player) ->
                    FormattingUtil.formatScoreboard(
                            FormattingUtil.GENERAL_PREFIX,
                            ComponentUtil.getComponent("sidebar", (config.teamSize() > 1 ? "teams" : "players"),
                                    Component.literal(String.valueOf(liveTeams.keySet().size())).withStyle(ChatFormatting.WHITE)
                            ).withStyle(ChatFormatting.GREEN)
                    )
            );

            b.add(CommonComponents.EMPTY);

            b.add((player) -> {
                if (player != null) {
                    SkyWarsPlayer data = this.participants.get(PlayerRef.of(player));

                    if (data != null) {
                        return FormattingUtil.formatScoreboard(
                                FormattingUtil.DEATH_PREFIX,
                                Style.EMPTY.withColor(ChatFormatting.GOLD),
                                ComponentUtil.getComponent("sidebar", "kills",
                                        Component.literal("" + data.kills).withStyle(ChatFormatting.WHITE)
                                )
                        );
                    }
                }
                return CommonComponents.EMPTY;
            });

            b.add(CommonComponents.EMPTY);

            b.add((player) -> {
                var time = Math.max(stageManager.refillTime - level.getGameTime(), 0);
                return FormattingUtil.formatScoreboard(
                        FormattingUtil.TIME_PREFIX,
                        Style.EMPTY.withColor(ChatFormatting.GREEN),
                        ComponentUtil.getComponent("sidebar", "refill",
                                Component.literal(formatTime(time)).withStyle(ChatFormatting.WHITE))
                );
            });
            b.add((player) -> {
                var time = Math.max(stageManager.finishTime - level.getGameTime(), 0);
                return FormattingUtil.formatScoreboard(
                        FormattingUtil.COMET_PREFIX,
                        Style.EMPTY.withColor(ChatFormatting.GREEN),
                        ComponentUtil.getComponent("sidebar", "armageddon",
                                Component.literal(formatTime(time)).withStyle(ChatFormatting.WHITE))
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

    public static class WinResult {
        final GameTeam winningTeam;
        final boolean win;

        private WinResult(GameTeam winningTeam, boolean win) {
            this.winningTeam = winningTeam;
            this.win = win;
        }

        static WinResult no() {
            return new WinResult(null, false);
        }

        static WinResult win(GameTeam team) {
            return new WinResult(team, true);
        }

        public boolean isWin() {
            return this.win;
        }

        public GameTeam getWinningTeam() {
            return this.winningTeam;
        }
    }
}
