package me.gimme.gimmetag.tag;

import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.events.PlayerRoleSetEvent;
import me.gimme.gimmetag.events.PlayerTaggedEvent;
import me.gimme.gimmetag.events.TagStartEvent;
import me.gimme.gimmetag.item.ItemManager;
import me.gimme.gimmetag.sfx.SoundEffect;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TagManager implements Listener {

    private static final ChatColor INFO_MESSAGE_COLOR = ChatColor.YELLOW;

    private Plugin plugin;
    private Server server;
    private TagScoreboard tagScoreboard;
    private ItemManager itemManager;
    private InventorySupplier inventorySupplier;

    private Set<UUID> desiredHunters = new HashSet<>(); // Players that want to be hunters next round
    private Map<UUID, GameplayState> previousStateByPlayer = new HashMap<>(); // Stores previous states during round

    private Map<UUID, ItemStack[]> logoutItemsByPlayer = new HashMap<>(); // Stores inventories if quit during round
    private Map<UUID, @Nullable Role> logoutRoleByPlayer = new HashMap<>(); // Stores roles if quit during round

    // Active round
    private Map<UUID, @Nullable Role> roleByPlayer = new HashMap<>();
    private Set<UUID> hunters = new HashSet<>(); // Current hunters
    private Set<UUID> runners = new HashSet<>(); // Current runners
    private Map<UUID, BukkitRunnable> sleepingPlayers = new HashMap<>(); // For new hunters
    private static Map<UUID, BukkitRunnable> countdownTasks = new HashMap<>();
    @Nullable
    private BukkitRunnable pointsTicker = null;

    // If there is an active round of tag
    private boolean activeRound = false;

    public TagManager(@NotNull Plugin plugin, @NotNull ItemManager itemManager) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.itemManager = itemManager;
        this.inventorySupplier = new InventorySupplier(itemManager);

        tagScoreboard = new TagScoreboard(server);
        server.getPluginManager().registerEvents(tagScoreboard, plugin);
    }

    public void onDisable() {
        stop(false);
    }

    /**
     * Marks the specified player with priority to become hunter or runner for the next round.
     *
     * @param player the player to mark priority for
     * @param hunter if the player should be marked as hunter, else runner
     */
    public void setDesiredHunter(@NotNull Player player, boolean hunter) {
        Role role;
        if (hunter) {
            role = Role.HUNTER;
            if (!desiredHunters.add(player.getUniqueId())) {
                player.sendMessage(INFO_MESSAGE_COLOR + "You have already selected " + role.getDisplayName());
                return;
            }
        } else {
            role = Role.RUNNER;
            if (!desiredHunters.remove(player.getUniqueId())) {
                player.sendMessage(INFO_MESSAGE_COLOR + "You have already selected " + role.getDisplayName());
                return;
            }
        }

        String formatting = "" + ChatColor.GRAY + ChatColor.ITALIC;
        server.broadcastMessage(formatting + player.getDisplayName() + formatting + " wants to be " + role.getDisplayName());
    }

    public boolean isActiveRound() {
        return activeRound;
    }

    /**
     * Starts a new round of tag with randomly selected hunters.
     *
     * @param levelsToWin     the amount of levels required to win
     * @param sleepSeconds    the delay in seconds before the hunters can start chasing
     * @param numberOfHunters the amount of hunters to randomly select
     * @return if a new round of tag was successfully started
     */
    public boolean start(int levelsToWin, int sleepSeconds, int numberOfHunters) {
        if (server.getOnlinePlayers().size() < numberOfHunters) {
            Bukkit.getLogger().warning("Not enough players online");
            return false;
        }

        if (activeRound) return false;

        Set<UUID> hunters;
        if (desiredHunters.size() >= numberOfHunters) {
            List<UUID> desiredHuntersCopy = new LinkedList<>(desiredHunters);
            Collections.shuffle(desiredHuntersCopy);
            hunters = new HashSet<>(desiredHuntersCopy.subList(0, numberOfHunters));
        } else {
            // Pick from desired hunters
            hunters = new HashSet<>(desiredHunters);

            // Pick from desired runners
            int missingHunters = numberOfHunters - hunters.size();
            List<UUID> remainingPlayers = server.getOnlinePlayers().stream()
                    .map(Entity::getUniqueId)
                    .filter(uuid -> !desiredHunters.contains(uuid))
                    .collect(Collectors.toCollection(LinkedList::new));
            Collections.shuffle(remainingPlayers);
            hunters.addAll(remainingPlayers.subList(0, missingHunters));
        }

        return start(levelsToWin, sleepSeconds, hunters);
    }

    /**
     * Starts a new round of tag with selected hunters.
     *
     * @param levelsToWin   the amount of levels required to win
     * @param sleepSeconds  the delay in seconds before the hunters can start chasing
     * @param chosenHunters the players that should start has hunters
     * @return if a new round of tag was successfully started
     */
    private boolean start(int levelsToWin, int sleepSeconds, @NotNull Set<UUID> chosenHunters) {
        TagStartEvent event = new TagStartEvent();
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        activeRound = true;
        clearDesiredRoles();

        // Assign roles
        for (Player player : server.getOnlinePlayers()) {
            if (chosenHunters.contains(player.getUniqueId())) setRole(player, Role.HUNTER);
            else setRole(player, Role.RUNNER);
        }

        for (UUID h : hunters) {
            Player hunter = server.getPlayer(h);
            if (hunter == null) continue;
            applySleep(hunter, sleepSeconds, Role.HUNTER.getDisplayName());
        }
        new CountdownTimerTask(plugin, sleepSeconds) {
            @Override
            protected void onCount() {
                Function<Long, String> title = i -> "Hunters waking up in " + i + "s";
                Function<Long, Boolean> shouldDisplay = i -> i > sleepSeconds - 3;

                runners.stream()
                        .map(r -> server.getPlayer(r))
                        .filter(Objects::nonNull)
                        .filter(OfflinePlayer::isOnline)
                        .forEach(p -> {
                            if (shouldDisplay.apply(getSeconds())) {
                                p.sendTitle(Role.RUNNER.getDisplayName(),
                                        title.apply(getSeconds()), 0, 25, 10);
                            } else {
                                p.sendTitle(Role.RUNNER.getDisplayName(),
                                        title.apply(getSeconds()), 0, 0, 12);
                            }
                        });

                if (!shouldDisplay.apply(getSeconds())) {
                    finish();
                }
            }
        }.start();
        new CountdownTimerTask(plugin, sleepSeconds) {
            @Override
            protected void onCount() {
                if (getSeconds() <= 3) {
                    roleByPlayer.keySet().stream()
                            .map(uuid -> server.getPlayer(uuid))
                            .filter(Objects::nonNull)
                            .forEach(p -> {
                                SoundEffect.COUNTDOWN.play(p);
                                if (Role.RUNNER.equals(getRole(p))) p.sendTitle("", getSeconds() + "", 0, 25, 10);
                            });
                }
            }

            @Override
            protected void onFinish() {
                roleByPlayer.keySet().stream()
                        .map(uuid -> server.getPlayer(uuid))
                        .filter(Objects::nonNull)
                        .forEach(p -> {
                            SoundEffect.COUNTDOWN_FINISH.play(p);
                            if (Role.RUNNER.equals(getRole(p))) p.sendTitle("", "They're coming!", 0, 25, 10);
                        });
            }
        }.start();

        tagScoreboard.setLevelsToWin(levelsToWin);

        pointsTicker = new BukkitRunnable() {
            @Override
            public void run() {
                int pointsPerTick = Config.SCORING_POINTS_PER_TICK.getValue();
                int maxDistance = Config.SCORING_HUNTER_DISTANCE.getValue();
                double maxDistanceSquared = Math.pow(maxDistance, 2);

                boolean checkDistance = maxDistance >= 0;

                for (Player runner : getOnlineRunners()) {
                    if (checkDistance) {
                        Player closestHunter = getClosestHunter(runner, false);
                        if (closestHunter == null) return; // No hunters

                        double distanceSquared = runner.getLocation().distanceSquared(closestHunter.getLocation());
                        if (distanceSquared > maxDistanceSquared) continue; // Too far away
                    }

                    tagScoreboard.addPoints(runner, pointsPerTick);
                }

                if (tagScoreboard.getWinner() != null) stop(true);
            }
        };
        pointsTicker.runTaskTimer(plugin, Config.SCORING_PERIOD.getValue(), Config.SCORING_PERIOD.getValue());

        return true;
    }

    /**
     * Stops the current round of tag.
     *
     * @param printScores if the scores of the round should be broadcast in chat
     * @return if an active round was successfully stopped
     */
    public boolean stop(boolean printScores) {
        if (!activeRound) return false;
        activeRound = false;

        broadcastGameOverTitle(tagScoreboard.getWinner());
        // Display the final scoreboard
        if (printScores) tagScoreboard.printScores();

        // Clear active countdown tasks
        for (BukkitRunnable task : countdownTasks.values()) {
            if (!task.isCancelled()) task.cancel();
        }
        countdownTasks.clear();

        // Clear active sleeping tasks
        for (BukkitRunnable task : sleepingPlayers.values()) {
            if (!task.isCancelled()) task.cancel();
        }
        sleepingPlayers.clear();

        if (pointsTicker != null) {
            pointsTicker.cancel();
            pointsTicker = null;
        }

        clearRoles();
        logoutItemsByPlayer.clear();
        logoutRoleByPlayer.clear();
        tagScoreboard.reset();

        return true;
    }

    /**
     * Sends a game over title message to all players showing who won the round.
     *
     * @param winner the player who won the round
     */
    private void broadcastGameOverTitle(@Nullable Player winner) {
        roleByPlayer.keySet().stream()
                .map(uuid -> server.getPlayer(uuid))
                .filter(Objects::nonNull)
                .forEach(p -> {
                    if (winner != null) {
                        p.sendTitle(
                                (p.equals(winner) ? ChatColor.GREEN : ChatColor.RED) + "GAME OVER",
                                winner.getDisplayName() + ChatColor.YELLOW + " won",
                                0,
                                60,
                                30);
                    } else {
                        p.sendTitle(
                                ChatColor.YELLOW + "GAME OVER",
                                "",
                                0,
                                60,
                                30);
                    }
                });
    }

    /**
     * Makes the specified hunter tag the specified runner.
     * <p>
     * The players will swap roles and have their inventories reset to the starting state of their new roles.
     *
     * @param runner the player that was tagged
     * @param hunter the player that tagged the runner
     */
    private void tag(@NotNull Player runner, @NotNull Player hunter) {
        PlayerTaggedEvent event = new PlayerTaggedEvent(runner, hunter);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        setRole(runner, Role.HUNTER);
        setRole(hunter, Role.RUNNER);

        int sleepSeconds = Config.TAG_SLEEP_TIME.getValue();
        applySleep(runner, sleepSeconds, Role.HUNTER.getColor() + "Tagged!");

        SoundEffect.TAG.play(hunter);
        SoundEffect.TAGGED.play(runner);

        tagScoreboard.addPoints(hunter, Config.SCORING_POINTS_ON_TAG.getValue());
        tagScoreboard.addPoints(runner, Config.SCORING_POINTS_ON_TAGGED.getValue());

        for (Player p : server.getOnlinePlayers()) {
            if (p == runner) continue;
            if (p == hunter) continue;

            server.broadcastMessage(ChatColor.RED + runner.getDisplayName() + ChatColor.RED + " was tagged!");
            SoundEffect.TAG_BROADCAST.play(p);
        }
    }

    /**
     * Applies sleep effects to a new hunter.
     *
     * @param player  the player to apply the effects to
     * @param seconds the duration of the sleep in seconds
     * @param title   the title of the "waking up in" message
     */
    private void applySleep(@NotNull Player player, int seconds, @NotNull String title) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                sleepingPlayers.remove(player.getUniqueId());
            }
        };
        sleepingPlayers.put(player.getUniqueId(), task);
        task.runTaskLater(plugin, seconds * 20);

        new PlayerCountdownTimerTask(plugin, seconds, player,
                title, "Waking up in",
                "Hunt!", "").start();

        UUID id = UUID.randomUUID();
        BukkitRunnable runnable = new BukkitRunnable() {
            UUID playerId = player.getUniqueId();
            int ticksLeft = seconds * 20;
            int potionTicks = 2;

            @Override
            public void run() {
                if (ticksLeft <= 0) {
                    cancel();
                    countdownTasks.remove(id);
                }
                ticksLeft--;

                Player player = server.getPlayer(playerId);
                if (player == null || !player.isOnline()) return;

                player.addPotionEffects(Arrays.asList(
                        new PotionEffect(PotionEffectType.BLINDNESS, potionTicks + 20, 1),
                        new PotionEffect(PotionEffectType.SLOW, potionTicks, 1000), // Prevents moving
                        new PotionEffect(PotionEffectType.JUMP, potionTicks, 200), // Prevents jumping
                        new PotionEffect(PotionEffectType.WATER_BREATHING, potionTicks + 5 * 20, 0),
                        new PotionEffect(PotionEffectType.FIRE_RESISTANCE, potionTicks + 5 * 20, 0),
                        new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, potionTicks, 1000),
                        new PotionEffect(PotionEffectType.SLOW_FALLING, potionTicks, 1),
                        new PotionEffect(PotionEffectType.GLOWING, potionTicks, 1)
                ));
            }
        };
        countdownTasks.put(id, runnable);
        runnable.runTaskTimer(plugin, 0, 1);
    }

    /**
     * Sets the role of the specified player.
     *
     * @param player the player to set the role of
     * @param role   the role to set the player to, or null to only unset the role
     */
    private void setRole(@NotNull Player player, @Nullable Role role) {
        UUID uuid = player.getUniqueId();
        Role previousRole = getRole(player);

        if (Role.HUNTER.equals(previousRole)) hunters.remove(uuid);
        else if (Role.RUNNER.equals(previousRole)) runners.remove(uuid);

        if (Role.HUNTER.equals(role)) hunters.add(uuid);
        else if (Role.RUNNER.equals(role)) runners.add(uuid);

        if (role != null) {
            tagScoreboard.setTeam(player, role);
            roleByPlayer.put(uuid, role);
            storeGameplayState(player);
            applyStartingPlayerState(player, role);
        } else {
            roleByPlayer.remove(uuid);
            restoreGameplayState(player);
        }

        Bukkit.getPluginManager().callEvent(new PlayerRoleSetEvent(player, role));
    }

    /**
     * Apply the starting state of the specified player based on the role.
     *
     * @param player the player to apply the state on
     * @param role   the role to get the starting state from
     */
    private void applyStartingPlayerState(@NotNull Player player, @NotNull Role role) {
        // Add role color to name
        player.setDisplayName(role.playerDisplayName(player));
        // Set game mode
        player.setGameMode(GameMode.valueOf(Config.GAME_MODE.getValue()));
        // Clear inventory and then add items depending on role
        inventorySupplier.setInventory(player, role);
        // Fill food level
        player.setFoodLevel(20);
        // Clear XP
        player.setTotalExperience(0);
        // Clear potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    /**
     * Stores the gameplay state of the specified player, including gamemode, inventory and xp.
     *
     * @param player the player to store the gameplay state of
     */
    private void storeGameplayState(@NotNull Player player) {
        previousStateByPlayer.put(player.getUniqueId(), GameplayState.of(player));
    }

    /**
     * Restores the stored gameplay state of the specified player, including gamemode, inventory and xp.
     *
     * @param player the player to restore the gameplay state of
     */
    private void restoreGameplayState(@NotNull Player player) {
        GameplayState state = previousStateByPlayer.get(player.getUniqueId());

        if (state != null) {
            state.apply(player);
        } else {
            plugin.getLogger().warning("Missing stored player state");
            GameplayState.defaultState(player, server).apply(player);
        }
    }

    /**
     * Clears all assigned player roles.
     */
    private void clearRoles() {
        Iterator<UUID> iter = roleByPlayer.keySet().iterator();
        while (iter.hasNext()) {
            UUID uuid = iter.next();

            Player player = server.getPlayer(uuid);
            if (player == null) continue;
            setRole(player, null);
        }

        // Clear any offline players left
        roleByPlayer.clear();
        hunters.clear();
        runners.clear();
    }

    /**
     * Clears all desired roles by players.
     */
    private void clearDesiredRoles() {
        desiredHunters.clear();
    }

    /**
     * @param player the player to get the role of
     * @return the role of the specified player, or null if the player has no role
     */
    @Nullable
    private Role getRole(@NotNull OfflinePlayer player) {
        return roleByPlayer.get(player.getUniqueId());
    }

    /**
     * @param player the player to check
     * @return if the specified player is a hunter and is sleeping (by recently being tagged)
     */
    private boolean isSleeping(@NotNull Player player) {
        return sleepingPlayers.containsKey(player.getUniqueId());
    }

    /**
     * Keeps inventories on death.
     */
    @EventHandler
    private void onPlayerRespawn(PlayerDeathEvent event) {
        if (!activeRound) return;
        event.setKeepInventory(true);
    }

    /**
     * If a runner dies near a hunter, they become tagged (to prevent suicide abuse).
     */
    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!Role.RUNNER.equals(getRole(player))) return;

        Player closestHunter = getClosestHunter(player, false);
        if (closestHunter == null) return;
        double distance = player.getLocation().distance(closestHunter.getLocation());
        if (distance > Config.TAG_DEATH_DISTANCE.getValue()) return;

        tag(player, closestHunter);
    }

    /**
     * Handles hunters tagging runners, and disables melee damage between all players.
     */
    @EventHandler
    private void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!activeRound) return;

        Entity damagerEntity = event.getDamager();
        Entity damagedEntity = event.getEntity();
        // Return if it's not two players
        if (!(damagerEntity.getType().equals(EntityType.PLAYER) && damagedEntity.getType().equals(EntityType.PLAYER)))
            return;

        Player damager = (Player) damagerEntity;
        Player damaged = (Player) damagedEntity;
        // Cancel event if either is sleeping
        if (isSleeping(damager) || isSleeping(damaged)) {
            event.setCancelled(true);
            return;
        }

        // Tag if hunter hits runner
        if (Role.HUNTER.equals(getRole(damager)) && Role.RUNNER.equals(getRole(damaged))) {
            tag(damaged, damager);
        }

        if (!Config.ENABLE_PVP.getValue()) event.setCancelled(true);

        // No damage
        event.setDamage(0);
    }

    /**
     * Disables arrow damage between all players.
     */
    @EventHandler
    private void onPlayerDamageByArrow(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!activeRound) return;

        Entity damagerEntity = event.getDamager();
        Entity damagedEntity = event.getEntity();
        if (!damagedEntity.getType().equals(EntityType.PLAYER)) return;
        if (!(damagerEntity.getType().equals(EntityType.ARROW) || damagerEntity.getType().equals(EntityType.SPECTRAL_ARROW)))
            return;

        Projectile damagerProjectile = (Projectile) damagerEntity;
        ProjectileSource damagerSource = damagerProjectile.getShooter();
        if (!(damagerSource instanceof Player)) return;

        // No damage
        event.setDamage(0);
    }

    /**
     * Sets joining players to runners if there is an active round.
     * Restores inventories of returning players in the same round.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!activeRound) return;

        // Restore role if joining in the same round
        Role role = logoutRoleByPlayer.get(player.getUniqueId());
        if (role == null) role = Role.RUNNER; // Set new players to runners
        setRole(player, role);

        // Restore inventory if joining in the same round
        ItemStack[] logoutItems = logoutItemsByPlayer.get(player.getUniqueId());
        if (logoutItems != null) player.getInventory().setContents(logoutItems);
    }

    /**
     * Clears inventory if in the middle of a round and remembers it for if they return.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        desiredHunters.remove(uuid);

        Role role = getRole(player);
        // If in the middle of a round
        if (role != null) {
            PlayerInventory inventory = player.getInventory();
            logoutItemsByPlayer.put(uuid, inventory.getContents());
            logoutRoleByPlayer.put(uuid, role);
            setRole(player, null); // Clears inventory
        }
    }

    /**
     * @param player          the player to check nearby hunters of
     * @param includeSleeping if sleeping hunters should be included
     * @return the closest non-sleeping hunter to the specified player (not self), or null if no hunters
     */
    @Nullable
    public Player getClosestHunter(@NotNull Player player, boolean includeSleeping) {
        return getClosestPlayer(player, getOnlineHunters(includeSleeping));
    }

    /**
     * @param player the player to check nearby runners of
     * @return the closest runner to the specified player (not self), or null if no runners
     */
    @Nullable
    public Player getClosestRunner(@NotNull Player player) {
        return getClosestPlayer(player, getOnlineRunners());
    }

    /**
     * @param player the player to check nearby players of
     * @return the closest player to the specified player (not self), or null if list has no other players
     */
    @Nullable
    private Player getClosestPlayer(@NotNull Player player, @NotNull List<Player> players) {
        if (players.isEmpty()) return null;
        if (players.size() == 1 && players.get(0).getUniqueId().equals(player.getUniqueId())) return null;

        return Collections.min(players, Comparator.comparing((p) -> {
            if (p.getUniqueId().equals(player.getUniqueId())) return Double.MAX_VALUE;
            return player.getLocation().distanceSquared(p.getLocation());
        }));
    }

    /**
     * Returns all online hunters. If there is no ongoing round, this will return an empty list.
     *
     * @return all online hunters
     */
    public List<Player> getOnlineHunters() {
        return getOnlineHunters(true);
    }

    /**
     * Returns all online hunters with or without sleeping. If there is no ongoing round, this will return an empty list.
     *
     * @param includeSleeping if sleeping hunters should be included
     * @return all online hunters
     */
    public List<Player> getOnlineHunters(boolean includeSleeping) {
        return hunters.stream()
                .map(id -> server.getPlayer(id))
                .filter(hunter -> hunter != null && (includeSleeping || !isSleeping(hunter)))
                .collect(Collectors.toList());
    }

    /**
     * Returns all online runners. If there is no ongoing round, this will return an empty list.
     *
     * @return all online runners
     */
    public List<Player> getOnlineRunners() {
        return runners.stream()
                .map(id -> server.getPlayer(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * @return the internal set of hunters
     */
    public Set<UUID> getHunters() {
        return hunters;
    }

    /**
     * @return the internal set of runners
     */
    public Set<UUID> getRunners() {
        return runners;
    }


    private abstract static class CountdownTimerTask extends me.gimme.gimmecore.util.countdown.CountdownTimerTask {
        private UUID id = UUID.randomUUID();

        protected CountdownTimerTask(@NotNull Plugin plugin, long seconds) {
            super(plugin, seconds);
        }

        @Override
        protected void onFinish() {
            countdownTasks.remove(id);
        }

        @Override
        public me.gimme.gimmecore.util.countdown.@NotNull CountdownTimerTask start() {
            countdownTasks.put(id, this);
            return super.start();
        }
    }

    private static class PlayerCountdownTimerTask extends me.gimme.gimmecore.util.countdown.PlayerCountdownTimerTask {
        private UUID id = UUID.randomUUID();

        public PlayerCountdownTimerTask(@NotNull Plugin plugin, long seconds, @NotNull Player player, @Nullable String title, @Nullable String subtitle, @Nullable String finishTitle, @Nullable String finishSubtitle) {
            super(plugin, seconds, player, title, subtitle, finishTitle, finishSubtitle);
        }

        @Override
        protected void onFinish() {
            super.onFinish();
            countdownTasks.remove(id);
        }

        @Override
        public me.gimme.gimmecore.util.countdown.@NotNull CountdownTimerTask start() {
            countdownTasks.put(id, this);
            return super.start();
        }
    }
}
