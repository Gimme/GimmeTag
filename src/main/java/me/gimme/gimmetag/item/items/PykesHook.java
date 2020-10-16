package me.gimme.gimmetag.item.items;

import me.gimme.gimmetag.config.type.BouncyProjectileConfig;
import me.gimme.gimmetag.item.BowProjectileItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A bow whose arrows pull in anyone they hit. Like the League of Legends champion Pyke's Q ability.
 */
public class PykesHook extends BowProjectileItem {

    private static final String NAME = "Pyke's Hook";
    private static final Class<? extends Projectile> PROJECTILE_CLASS = Trident.class;

    public PykesHook(@NotNull String id, @NotNull BouncyProjectileConfig config, @NotNull Plugin plugin) {
        super(id, NAME, config, plugin);

        setProjectileClass(PROJECTILE_CLASS);
    }

    @Override
    protected void onExplode(@NotNull Projectile projectile, @NotNull Collection<@NotNull Entity> livingEntities) {
    }

    @Override
    protected void onHitEntity(@NotNull Projectile projectile, @NotNull Entity entity) {
        Vector velocity = projectile.getVelocity().clone();
        velocity.setY(0);
        velocity.normalize();
        velocity.multiply(-1);
        velocity.multiply(getPower());
        velocity.setY(0.5 + 0.01 * getPower());

        entity.setVelocity(velocity);
    }
}
