package me.gimme.gimmetag.item;

import me.gimme.gimmetag.config.type.BouncyProjectileConfig;
import me.gimme.gimmetag.item.entities.BouncyProjectile;
import me.gimme.gimmetag.sfx.PlayableSound;
import me.gimme.gimmetag.sfx.SoundEffects;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

public abstract class BouncyProjectileItem extends AbilityItem {

    private final Plugin plugin;
    private final BouncyProjectileConfig config;
    private final double speed;
    private final int maxExplosionTimerTicks;

    @Nullable
    private ItemStack displayItem;
    @Nullable
    private Particle trailParticle;
    @Nullable
    private PlayableSound explosionSound;

    public BouncyProjectileItem(@NotNull String name, @NotNull Material type, @NotNull BouncyProjectileConfig config,
                                @NotNull Plugin plugin) {
        super(name, type, config);

        this.plugin = plugin;
        this.config = config;
        this.speed = config.getSpeed();
        this.maxExplosionTimerTicks = Ticks.secondsToTicks(config.getMaxExplosionTimer());

        setUseSound(SoundEffects.THROW);
    }

    /**
     * Does something when the projectile explodes (finishes).
     *
     * @param projectile     the projectile that exploded
     * @param livingEntities the living entities that were in range of the explosion
     */
    protected abstract void onExplode(@NotNull Projectile projectile, @NotNull Collection<@NotNull Entity> livingEntities);

    /**
     * Does something when an entity gets hit directly by the projectile.
     *
     * @param projectile the projectile that hit the entity directly
     * @param entity     the entity that was hit
     */
    protected abstract void onHitEntity(@NotNull Projectile projectile, @NotNull Entity entity);

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        BouncyProjectile bouncyProjectile = BouncyProjectile.launch(plugin, user, speed, maxExplosionTimerTicks, displayItem);
        BouncyProjectileConfig.init(bouncyProjectile, config);

        bouncyProjectile.setOnExplode(this::onExplode);
        bouncyProjectile.setOnHitEntity(this::onHitEntity);
        bouncyProjectile.setExplosionSound(explosionSound);
        if (trailParticle != null) bouncyProjectile.setTrailParticle(trailParticle);

        return true;
    }

    protected void setDisplayItem(@NotNull Material material, boolean enchanted) {
        ItemStack itemStack = new ItemStack(material);

        if (enchanted) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            CustomItem.setGlowing(Objects.requireNonNull(itemMeta), true);
            itemStack.setItemMeta(itemMeta);
        }

        this.displayItem = itemStack;

    }

    protected void setExplosionSound(@NotNull PlayableSound explosionSound) {
        this.explosionSound = explosionSound;
    }

    protected void setTrailParticle(@NotNull Particle trailParticle) {
        this.trailParticle = trailParticle;
    }

    protected double getRadius() {
        return config.getRadius();
    }

    protected double getPower() {
        return config.getPower();
    }
}
