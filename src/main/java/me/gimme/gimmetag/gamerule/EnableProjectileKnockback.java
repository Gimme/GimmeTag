package me.gimme.gimmetag.gamerule;

import me.gimme.gimmetag.config.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class EnableProjectileKnockback implements Listener {

    private Condition condition;

    public EnableProjectileKnockback(@NotNull Condition condition) {
        this.condition = condition;
    }

    /**
     * Enables knockback on 0-damage projectiles.
     */
    @EventHandler
    private void onProjectileDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!condition.condition()) return;

        if (event.getDamage() > 0) return;
        if (!Config.ENABLE_KNOCKBACK.getValue().contains(event.getDamager().getType().getKey().getKey())) return;

        event.setDamage(0.0001); // Enables knockback without damage
    }
}
