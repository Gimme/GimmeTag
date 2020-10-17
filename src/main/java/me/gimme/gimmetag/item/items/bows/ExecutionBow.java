package me.gimme.gimmetag.item.items.bows;

import me.gimme.gimmetag.config.type.BouncyProjectileConfig;
import me.gimme.gimmetag.item.BowProjectileItem;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Bow that executes hit players if their health is below a certain threshold.
 */
public class ExecutionBow extends BowProjectileItem {

    private static final String NAME = "Execution Bow";

    public ExecutionBow(@NotNull String id, @NotNull BouncyProjectileConfig config, @NotNull Plugin plugin) {
        super(id, NAME, config, plugin);

        int percent = (int) Math.round(getPower() * 100);
        setInfo("Executes players at " + percent + "% health");
    }

    @Override
    protected void onExplode(@NotNull Projectile projectile, @NotNull Collection<@NotNull Entity> livingEntities) {
    }

    @Override
    protected void onHitEntity(@NotNull Projectile projectile, @NotNull LivingEntity entity) {
        AttributeInstance maxHealthAttribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute == null) return;

        double maxHealth = maxHealthAttribute.getValue();
        double health = entity.getHealth();

        if (health > getPower() * maxHealth + 0.001) return;

        // Execute target with credit
        entity.damage(health + 1, (Entity) projectile.getShooter());
    }
}
