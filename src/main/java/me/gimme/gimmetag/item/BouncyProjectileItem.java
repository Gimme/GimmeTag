package me.gimme.gimmetag.item;

import me.gimme.gimmetag.config.type.BouncyProjectileConfig;
import me.gimme.gimmetag.item.entities.BouncyProjectile;
import me.gimme.gimmetag.sfx.PlayableSound;
import me.gimme.gimmetag.sfx.SoundEffect;
import me.gimme.gimmetag.sfx.SoundEffects;
import me.gimme.gimmetag.sfx.StandardSoundEffect;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

public abstract class BouncyProjectileItem extends AbilityItem {

    private static final SoundEffect HIT_PLAYER_SOUND_EFFECT = new StandardSoundEffect(Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.NEUTRAL, 0.5f);

    private final Plugin plugin;
    private final BouncyProjectileConfig config;
    private final double speed;
    private final int maxExplosionTimerTicks;
    private final double radius;
    private final double power;

    @Nullable
    private Class<? extends Projectile> projectileClass;
    @Nullable
    private ItemStack displayItem;
    @Nullable
    private Particle trailParticle;
    @Nullable
    private PlayableSound explosionSound;
    private boolean hitSound = true;

    public BouncyProjectileItem(@NotNull String id, @NotNull String displayName, @NotNull Material type, @NotNull BouncyProjectileConfig config,
                                @NotNull Plugin plugin) {
        super(id, displayName, type, config);

        this.plugin = plugin;
        this.config = config;
        this.speed = config.getSpeed();
        this.maxExplosionTimerTicks = Ticks.secondsToTicks(config.getMaxExplosionTimer());
        this.radius = config.getRadius();
        this.power = config.getPower();

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
    protected abstract void onHitEntity(@NotNull Projectile projectile, @NotNull LivingEntity entity);

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        launch(user, 1);

        return true;
    }

    void launch(@NotNull Player launcher, double force) {
        BouncyProjectile bouncyProjectile;

        double realSpeed = force * speed;

        if (projectileClass != null) {
            Projectile projectile = launcher.launchProjectile(projectileClass);
            projectile.setVelocity(projectile.getVelocity().multiply(realSpeed));

            bouncyProjectile = new BouncyProjectile(plugin, projectile, launcher, maxExplosionTimerTicks);
        } else {
            bouncyProjectile = BouncyProjectile.launch(plugin, launcher, realSpeed, maxExplosionTimerTicks, displayItem);
        }

        init(bouncyProjectile);
    }

    private void init(@NotNull BouncyProjectile bouncyProjectile) {
        BouncyProjectileConfig.init(bouncyProjectile, config);

        bouncyProjectile.setOnExplode(this::onExplode);
        bouncyProjectile.setOnHitEntity((p, e) -> {
            onHitEntity(p, e);
            if (hitSound && e.getType() == EntityType.PLAYER && (p.getShooter() instanceof Player))
                HIT_PLAYER_SOUND_EFFECT.play((Player) p.getShooter());
        });
        bouncyProjectile.setExplosionSound(explosionSound);
        if (trailParticle != null) bouncyProjectile.setTrailParticle(trailParticle);
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

    protected void setProjectileClass(@Nullable Class<? extends Projectile> projectileClass) {
        this.projectileClass = projectileClass;
    }

    protected void setExplosionSound(@NotNull PlayableSound explosionSound) {
        this.explosionSound = explosionSound;
    }

    protected void setTrailParticle(@NotNull Particle trailParticle) {
        this.trailParticle = trailParticle;
    }

    protected void muteHitSound() {
        hitSound = false;
    }

    protected double getRadius() {
        return radius;
    }

    protected double getPower() {
        return power;
    }
}
