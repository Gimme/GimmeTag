package me.gimme.gimmetag.item;

import me.gimme.gimmetag.config.BouncyProjectileConfig;
import me.gimme.gimmetag.item.entities.BouncyProjectile;
import me.gimme.gimmetag.sfx.PlayableSound;
import me.gimme.gimmetag.sfx.SoundEffects;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public abstract class BouncyProjectileItem extends AbilityItem {

    private static final double CENTER_OF_GRAVITY_HEIGHT = 1;

    private final Plugin plugin;
    private final double speed;
    private final double gravity;
    private final int maxExplosionTimerTicks;
    private final int groundExplosionTimerTicks;
    private final double restitutionFactor;
    private final double frictionFactor;
    private final boolean trail;
    private final boolean bounceMarks;
    private final boolean glowing;
    private final double radius;
    private final double power;
    private final double damageOnDirectHit;
    private final boolean friendlyFire;
    private boolean consumeOnEntityHit;

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
        this.speed = config.getSpeed();
        this.gravity = config.getGravity();
        this.maxExplosionTimerTicks = Ticks.secondsToTicks(config.getMaxExplosionTimer());
        this.groundExplosionTimerTicks = Ticks.secondsToTicks(config.getGroundExplosionTimer());
        this.restitutionFactor = config.getRestitutionFactor();
        this.frictionFactor = config.getFrictionFactor();
        this.trail = config.getTrail();
        this.bounceMarks = config.getBounceMarks();
        this.glowing = config.getGlowing();
        this.radius = config.getRadius();
        this.power = config.getPower();
        this.damageOnDirectHit = config.getDirectHitDamage();
        this.friendlyFire = config.getFriendlyFire();

        setUseSound(SoundEffects.THROW);
    }

    protected abstract void onExplode(@NotNull Projectile projectile, @NotNull Collection<Entity> livingEntities);

    protected abstract void onHitEntity(@NotNull Projectile projectile, @NotNull Entity entity);

    @Override
    protected boolean onUse(@NotNull ItemStack itemStack, @NotNull Player user) {
        BouncyProjectile bouncyProjectile = BouncyProjectile.launch(plugin, user, speed, maxExplosionTimerTicks, displayItem);

        bouncyProjectile.setOnExplode((projectile) -> {
            if (explosionSound != null) explosionSound.play(projectile.getLocation());

            Location location = projectile.getLocation().clone();
            double extendedRadius = radius + 3;
            Collection<Entity> nearbyLivingEntities = radius <= 0 ? new ArrayList<>() : projectile.getWorld().getNearbyEntities(
                    location, extendedRadius, extendedRadius, extendedRadius,
                    e -> {
                        if (!friendlyFire && fromSameTeam(user, e)) return false;

                        double halfHeight = e.getHeight() / 2;

                        location.subtract(0, halfHeight, 0);
                        boolean inRange = e.getType().isAlive() && e.getLocation().distanceSquared(location) <= radius * radius;
                        location.add(0, halfHeight, 0);

                        return inRange;
                    }
            );

            onExplode(projectile, nearbyLivingEntities);
        });
        bouncyProjectile.setOnHitEntity(this::onHitEntity);
        bouncyProjectile.setGroundExplosionTimerTicks(groundExplosionTimerTicks);
        bouncyProjectile.setGravity(gravity);
        bouncyProjectile.setRestitutionFactor(restitutionFactor);
        bouncyProjectile.setFrictionFactor(frictionFactor);
        bouncyProjectile.setTrail(trail);
        bouncyProjectile.setBounceMarks(bounceMarks);
        bouncyProjectile.setGlowing(glowing);
        bouncyProjectile.setDamageOnDirectHit(damageOnDirectHit);
        bouncyProjectile.setConsumeOnDirectHit(consumeOnEntityHit);
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

    protected void setConsumeOnEntityHit(boolean consumeOnEntityHit) {
        this.consumeOnEntityHit = consumeOnEntityHit;
    }

    protected void setTrailParticle(@NotNull Particle trailParticle) {
        this.trailParticle = trailParticle;
    }

    protected double getRadius() {
        return radius;
    }

    protected double getPower() {
        return power;
    }


    private static boolean fromSameTeam(@NotNull Player player, @NotNull Entity other) {
        Scoreboard scoreboard = player.getScoreboard();
        Team team1 = scoreboard.getEntryTeam(player.getName());
        Team team2 = scoreboard.getEntryTeam(other.getName());
        return team1 != null && team1.equals(team2);
    }
}
