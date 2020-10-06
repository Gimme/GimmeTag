package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.AbilityItemConfig;
import me.gimme.gimmetag.item.AbilityItem;
import me.gimme.gimmetag.sfx.SoundEffects;
import me.gimme.gimmetag.tag.TagManager;
import org.apache.commons.collections4.map.LinkedMap;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SwapperBall extends AbilityItem {

    private static final String NAME = ChatColor.LIGHT_PURPLE + "Swapper Ball";
    private static final Material MATERIAL = Material.SNOWBALL;
    private static final List<String> INFO = Collections.singletonList("Swap positions with the hit player");
    private static final EntityType PROJECTILE_TYPE = EntityType.SNOWBALL; // Has to match the material above
    private static final Class<? extends Projectile> PROJECTILE_CLASS = Snowball.class;

    private final boolean allowHunterSwap;
    private final TagManager tagManager;
    private final OnHitListener onHitListener = new OnHitListener();

    public SwapperBall(@NotNull AbilityItemConfig config, boolean allowHunterSwap, @NotNull Plugin plugin,
                       @NotNull TagManager tagManager) {
        super(NAME, MATERIAL, config);

        this.allowHunterSwap = allowHunterSwap;
        this.tagManager = tagManager;

        setInfo(INFO);
        setUseSound(SoundEffects.THROW);

        plugin.getServer().getPluginManager().registerEvents(onHitListener, plugin);
    }

    @Override
    protected void onCreate(@NotNull ItemStack itemStack, @NotNull ItemMeta itemMeta) {
    }

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        Projectile projectile = user.launchProjectile(PROJECTILE_CLASS);
        projectile.setShooter(user);
        onHitListener.onLaunch(projectile);

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

        if (shooter.getType() == EntityType.PLAYER) playSound((Player) shooter);
        if (hit.getType() == EntityType.PLAYER) playSound((Player) hit);

        shooter.teleport(hitLocation);
        hit.teleport(shooterLocation);
    }


    private static void playSound(@NotNull Player player) {
        SoundEffects.TELEPORT.play(player);
    }


    private class OnHitListener implements Listener {

        private static final long TIME_TO_LIVE = 7 * 1000; // 7 seconds

        private final LinkedMap<@NotNull UUID, @NotNull Long> launchedProjectiles = new LinkedMap<>();

        /**
         * Registers launched projectiles.
         * <p>
         * This is necessary to differentiate normal projectiles to special ones, spawning from this item type, so that
         * we don't do the swapping for normal items.
         */
        private void onLaunch(@NotNull Projectile projectile) {
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
         * Detects hits.
         */
        @EventHandler(priority = EventPriority.MONITOR)
        private void onHit(ProjectileHitEvent event) {
            Projectile projectile = event.getEntity();
            Entity hitEntity = event.getHitEntity();

            if (projectile.getType() != PROJECTILE_TYPE) return; // Not the right type of projectile
            if (hitEntity == null) return; // Didn't hit
            if (hitEntity.getType() != EntityType.PLAYER) return; // Didn't hit a player
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

            if (hitEntity.getType() != EntityType.PLAYER) return; // Didn't hit a player
            if (damager.getType() != PROJECTILE_TYPE) return; // Not the right type of entity

            Projectile projectile = (Projectile) damager;
            if (!launchedProjectiles.containsKey(projectile.getUniqueId())) return; // Normal item

            event.setCancelled(true);
        }
    }
}
