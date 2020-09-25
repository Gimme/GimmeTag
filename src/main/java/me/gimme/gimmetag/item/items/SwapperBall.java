package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.item.AbilityItem;
import me.gimme.gimmetag.sfx.SoundEffect;
import me.gimme.gimmetag.tag.TagManager;
import org.apache.commons.collections4.map.LinkedMap;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SwapperBall extends AbilityItem {

    private static final Material MATERIAL = Material.SNOWBALL;
    private static final EntityType PROJECTILE_TYPE = EntityType.SNOWBALL; // Has to match the material above
    private static final String DISPLAY_NAME = ChatColor.LIGHT_PURPLE + "Swapper Ball";
    private static final List<String> LORE = Collections.singletonList("Swap positions with a player");

    private boolean allowHunterSwap;
    private boolean consumable;
    private TagManager tagManager;

    public SwapperBall(double cooldown, boolean consumable, boolean allowHunterSwap, @NotNull Plugin plugin,
                       @NotNull TagManager tagManager) {
        super(
                ChatColor.LIGHT_PURPLE + "Swapper Ball",
                MATERIAL,
                true,
                cooldown,
                false,
                null
        );

        this.consumable = consumable;
        this.allowHunterSwap = allowHunterSwap;
        this.tagManager = tagManager;

        plugin.getServer().getPluginManager().registerEvents(
                new OnHitListener(), plugin);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
        mute();
        itemMeta.setLore(LORE);
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        // Re-add the thrown projectile to your inventory if not consumable
        if (!consumable && !user.getGameMode().equals(GameMode.CREATIVE))
            itemStack.setAmount(itemStack.getAmount() + 1);
        return true;
    }

    private void swap(@NotNull LivingEntity shooter, @NotNull LivingEntity hit) {
        // Don't allow swapping with hunters if disabled
        if (!allowHunterSwap && tagManager.getHunters().contains(hit.getUniqueId())) return;

        // Don't swap if either is dead
        if (shooter.isDead() || hit.isDead()) return;

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


    private class OnHitListener implements Listener {

        private static final long TIME_TO_LIVE = 7 * 1000; // 7 seconds

        private LinkedMap<@NotNull UUID, @NotNull Long> launchedProjectiles = new LinkedMap<>();

        /**
         * Registers launched projectiles (necessary to differentiate normal projectiles to special ones
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

            launchedProjectiles.put(projectile.getUniqueId(), System.currentTimeMillis());
            cleanUp();
        }

        private void cleanUp() {
            long currentTimeMillis = System.currentTimeMillis();
            boolean cleanupCompleted = false;
            while (!cleanupCompleted) {
                UUID key = launchedProjectiles.lastKey();
                Long timestamp = launchedProjectiles.get(key);

                if (currentTimeMillis - timestamp <= TIME_TO_LIVE) {
                    cleanupCompleted = true;
                } else {
                    launchedProjectiles.remove(key);
                }
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

            swap(shooter, hitPlayer);
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
    }
}
