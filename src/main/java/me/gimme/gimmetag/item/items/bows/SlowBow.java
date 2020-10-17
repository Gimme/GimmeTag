package me.gimme.gimmetag.item.items.bows;

import me.gimme.gimmetag.config.type.BouncyProjectileConfig;
import me.gimme.gimmetag.item.BowProjectileItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Bow that slows with its arrows.
 */
public class SlowBow extends BowProjectileItem {

    private static final String NAME = "Slow Bow";

    public SlowBow(@NotNull String id, @NotNull BouncyProjectileConfig config, @NotNull Plugin plugin) {
        super(id, NAME, config, plugin);

        setInfo("Slows on hit");
    }

    @Override
    protected void onExplode(@NotNull Projectile projectile, @NotNull Collection<@NotNull Entity> livingEntities) {
    }

    @Override
    protected void onHitEntity(@NotNull Projectile projectile, @NotNull LivingEntity entity) {
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, getDurationTicks(), getAmplifier()));
    }
}
