package me.gimme.gimmetag.gamerule;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/**
 * Prevents arrows from dealing damage.
 */
public class DisableArrowDamage implements Listener {

    private final BooleanSupplier condition;

    public DisableArrowDamage(@NotNull BooleanSupplier condition) {
        this.condition = condition;
    }

    /**
     * Disables arrow damage between all players (still has a knockback).
     */
    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerDamageByArrow(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!condition.getAsBoolean()) return;

        Entity damagerEntity = event.getDamager();
        Entity damagedEntity = event.getEntity();
        if (damagedEntity.getType() != EntityType.PLAYER) return;
        if (!(damagerEntity.getType() == EntityType.ARROW || damagerEntity.getType() == EntityType.SPECTRAL_ARROW))
            return;

        Projectile damagerProjectile = (Projectile) damagerEntity;
        ProjectileSource damagerSource = damagerProjectile.getShooter();
        if (!(damagerSource instanceof Player)) return;

        // No damage
        event.setDamage(0);
    }
}
