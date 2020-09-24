package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.GimmeTag;
import me.gimme.gimmetag.config.Config;
import me.gimme.gimmetag.events.PlayerTaggedEvent;
import me.gimme.gimmetag.item.CustomItem;
import me.gimme.gimmetag.sfx.SoundEffect;
import org.apache.commons.collections4.map.LinkedMap;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SwapperBall extends CustomItem {

    private static boolean registeredEvents = false;

    private static final String NAME = "swapper_ball";
    private static final Material MATERIAL = Material.SNOWBALL;
    private static final EntityType PROJECTILE_TYPE = EntityType.SNOWBALL; // Has to match the material above
    private static final String DISPLAY_NAME = ChatColor.LIGHT_PURPLE + "Swapper Ball";
    private static final List<String> LORE = Collections.singletonList("Swap positions with a player");

    public SwapperBall() {
        this(1);
    }

    public SwapperBall(int amount) {
        super(NAME, MATERIAL, amount);

        ItemMeta meta = Objects.requireNonNull(getItemMeta());
        meta.setDisplayName(DISPLAY_NAME);
        meta.setLore(LORE);
        meta.addEnchant(Enchantment.LOYALTY, 1, true);
        setItemMeta(meta);

        Plugin plugin = GimmeTag.getPlugin();
        if (!registeredEvents) {
            registeredEvents = true;
            plugin.getServer().getPluginManager().registerEvents(
                    new OnHitListener(plugin, Math.round(Config.SWAPPER_BALL_COOLDOWN.getValue().doubleValue() * 20)), plugin);
        }
    }

    private static void swap(@NotNull LivingEntity shooter, @NotNull LivingEntity hit) {
        // Don't allow swapping with hunters
        if (!Config.SWAPPER_ALLOW_HUNTER_SWAP.getValue()
                && GimmeTag.getPlugin().getTagManager().getHunters().contains(hit.getUniqueId())) return;

        Location shooterLocation = shooter.getLocation();
        Location hitLocation = hit.getLocation();

        shooterLocation.setDirection(hitLocation.toVector().subtract(shooterLocation.toVector()));
        hitLocation.setDirection(shooterLocation.toVector().subtract(hitLocation.toVector()));

        if (shooter.getType().equals(EntityType.PLAYER)) playSound((Player) shooter);
        if (hit.getType().equals(EntityType.PLAYER)) playSound((Player) hit);

        shooter.teleport(hitLocation);
        hit.teleport(shooterLocation);
    }


    private static void playSound(@NotNull Player player) {
        SoundEffect.TELEPORT.play(player);
    }

    private static boolean isSwapperBall(@NotNull ItemStack item) {
        if (!item.getType().equals(MATERIAL)) return false;

        String itemName = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
        return itemName.equals(DISPLAY_NAME);
    }


    private static class OnHitListener implements Listener {

        private static final long TIME_TO_LIVE = 7 * 1000; // 7 seconds

        private Plugin plugin;
        private long returnToInventory;

        private LinkedMap<@NotNull UUID, @NotNull Long> launchedProjectiles = new LinkedMap<>();
        private Map<UUID, List<BukkitRunnable>> returnToInventoryTasksByPlayer = new HashMap<>();

        public OnHitListener(@NotNull Plugin plugin, long returnToInventory) {
            this.plugin = plugin;
            this.returnToInventory = returnToInventory;
        }

        /**
         * Registers launched projectiles (necessasry to differentiate normal projectiles vs special ones
         * spawning from this item type), and returns the item to inventory after a delay.
         */
        @EventHandler(priority = EventPriority.MONITOR)
        private void onLaunch(ProjectileLaunchEvent event) {
            Projectile projectile = event.getEntity();

            if (!projectile.getType().equals(PROJECTILE_TYPE)) return; // Not the right type of projectile
            if (!(projectile.getShooter() instanceof Player)) return; // Not a player that launched the projectile

            Player shooter = (Player) projectile.getShooter();

            ItemStack item;
            ItemStack mainHand = shooter.getInventory().getItemInMainHand();
            ItemStack offHand = shooter.getInventory().getItemInOffHand();
            if (mainHand.getType().equals(MATERIAL) && offHand.getType().equals(MATERIAL)) {
                item = mainHand;
            } else {
                item = mainHand.getType().equals(MATERIAL) ? mainHand : offHand;
            }

            if (!isSwapperBall(item)) return;

            // Return item to inventory after a delay
            if (returnToInventory >= 0 && !shooter.getGameMode().equals(GameMode.CREATIVE)) {
                List<BukkitRunnable> returnToInventoryTasks = returnToInventoryTasksByPlayer.computeIfAbsent(shooter.getUniqueId(),
                        k -> new ArrayList<>());
                BukkitRunnable task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        shooter.getInventory().addItem(new SwapperBall())
                                .values().forEach(item -> shooter.getWorld().dropItemNaturally(shooter.getLocation(), item));

                        returnToInventoryTasks.remove(this);
                    }
                };
                returnToInventoryTasks.add(task);
                task.runTaskLater(plugin, returnToInventory);
            }

            launchedProjectiles.put(projectile.getUniqueId(), System.currentTimeMillis());
            cleanUp();
        }

        private void cleanUp() {
            long currentTimeMillis = System.currentTimeMillis();
            for (; ; ) {
                UUID key = launchedProjectiles.lastKey();
                Long timestamp = launchedProjectiles.get(key);
                if (currentTimeMillis - timestamp <= TIME_TO_LIVE) break;

                launchedProjectiles.remove(key);
            }
        }

        /**
         * Detects hit.
         */
        @EventHandler(priority = EventPriority.MONITOR)
        private void onHit(ProjectileHitEvent event) {
            Projectile projectile = event.getEntity();
            Entity hitEntity = event.getHitEntity();

            if (!projectile.getType().equals(PROJECTILE_TYPE)) return; // Not the right type of projectile
            if (hitEntity == null) return; // Didn't hit
            if (!hitEntity.getType().equals(EntityType.PLAYER)) return; // Didn't hit a player
            if (!launchedProjectiles.containsKey(projectile.getUniqueId())) return; // Normal item

            LivingEntity shooter = (LivingEntity) projectile.getShooter();
            LivingEntity hitPlayer = (LivingEntity) hitEntity;

            if (shooter == null) return;

            SwapperBall.swap(shooter, hitPlayer);
        }

        /**
         * Disables knockback.
         */
        @EventHandler
        private void onDamagePlayer(EntityDamageByEntityEvent event) {
            if (event.isCancelled()) return;

            Entity damager = event.getDamager();
            Entity hitEntity = event.getEntity();

            if (!hitEntity.getType().equals(EntityType.PLAYER)) return; // Didn't hit a player
            if (!damager.getType().equals(PROJECTILE_TYPE)) return; // Not the right type of entity

            Projectile projectile = (Projectile) damager;
            if (!launchedProjectiles.containsKey(projectile.getUniqueId())) return; // Normal item

            event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private void onDeath(PlayerDeathEvent event) {
            clearTasks(event.getEntity());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        private void onPlayerTagged(PlayerTaggedEvent event) {
            if (event.isCancelled()) return;
            clearTasks(event.getPlayer());
        }

        private void clearTasks(@NotNull Player player) {
            List<BukkitRunnable> returnToInventoryTasks = returnToInventoryTasksByPlayer.get(player.getUniqueId());
            if (returnToInventoryTasks == null) return;

            for (BukkitRunnable task : returnToInventoryTasks) {
                task.cancel();
                task.run();
            }
        }
    }
}
