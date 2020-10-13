package me.gimme.gimmetag.config;

import me.gimme.gimmetag.item.entities.BouncyProjectile;
import me.gimme.gimmetag.utils.Ticks;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BouncyProjectileConfig extends AbilityItemConfig {
    private static final String SPEED_PATH = "speed";
    private static final String GRAVITY_PATH = "gravity";
    private static final String MAX_EXPLOSION_TIMER_PATH = "max-explosion-timer";
    private static final String GROUND_EXPLOSION_TIMER_PATH = "ground-explosion-timer";
    private static final String RESTITUTION_FACTOR_PATH = "restitution-factor";
    private static final String FRICTION_FACTOR_PATH = "friction-factor";
    private static final String STICKY_PATH = "sticky";
    private static final String GLOWING_PATH = "glowing";
    private static final String TRAIL_PATH = "trail";
    private static final String BOUNCE_MARKS_PATH = "bounce-marks";
    private static final String RADIUS_PATH = "radius";
    private static final String POWER_PATH = "power";
    private static final String DIRECT_HIT_DAMAGE_PATH = "direct-hit-damage";
    private static final String CONSUME_ON_DIRECT_HIT_PATH = "consume-on-direct-hit";
    private static final String FRIENDLY_FIRE_PATH = "friendly-fire";

    @Nullable
    private final BouncyProjectileConfig defaultConfig;

    BouncyProjectileConfig(@NotNull AbstractConfig<ConfigurationSection> parent, @NotNull String path,
                           @Nullable BouncyProjectileConfig defaultConfig) {
        super(parent, path);
        this.defaultConfig = defaultConfig;
    }

    BouncyProjectileConfig(@NotNull ConfigurationSection parent, @NotNull String path, @Nullable BouncyProjectileConfig defaultConfig) {
        super(parent, path);
        this.defaultConfig = defaultConfig;
    }

    public double getSpeed() {
        return getValue().getDouble(SPEED_PATH, defaultConfig != null ? defaultConfig.getSpeed() : 0);
    }

    public double getGravity() {
        return getValue().getDouble(GRAVITY_PATH, defaultConfig != null ? defaultConfig.getGravity() : 0);
    }

    public double getMaxExplosionTimer() {
        return getValue().getDouble(MAX_EXPLOSION_TIMER_PATH, defaultConfig != null ? defaultConfig.getMaxExplosionTimer() : 0);
    }

    public double getGroundExplosionTimer() {
        return getValue().getDouble(GROUND_EXPLOSION_TIMER_PATH, defaultConfig != null ? defaultConfig.getGroundExplosionTimer() : 0);
    }

    public double getRestitutionFactor() {
        return getValue().getDouble(RESTITUTION_FACTOR_PATH, defaultConfig != null ? defaultConfig.getRestitutionFactor() : 0);
    }

    public double getFrictionFactor() {
        return getValue().getDouble(FRICTION_FACTOR_PATH, defaultConfig != null ? defaultConfig.getFrictionFactor() : 0);
    }

    public boolean isSticky() {
        return getValue().getBoolean(STICKY_PATH, defaultConfig != null && defaultConfig.isSticky());
    }

    public boolean getTrail() {
        return getValue().getBoolean(TRAIL_PATH, defaultConfig != null && defaultConfig.getTrail());
    }

    public boolean getBounceMarks() {
        return getValue().getBoolean(BOUNCE_MARKS_PATH, defaultConfig != null && defaultConfig.getBounceMarks());
    }

    public boolean getGlowing() {
        return getValue().getBoolean(GLOWING_PATH, defaultConfig != null && defaultConfig.getGlowing());
    }

    public double getRadius() {
        return getValue().getDouble(RADIUS_PATH, defaultConfig != null ? defaultConfig.getRadius() : 0);
    }

    public double getPower() {
        return getValue().getDouble(POWER_PATH, defaultConfig != null ? defaultConfig.getPower() : 0);
    }

    public double getDirectHitDamage() {
        return getValue().getDouble(DIRECT_HIT_DAMAGE_PATH, defaultConfig != null ? defaultConfig.getDirectHitDamage() : 0);
    }

    public boolean getConsumeOnDirectHit() {
        return getValue().getBoolean(CONSUME_ON_DIRECT_HIT_PATH, defaultConfig != null && defaultConfig.getConsumeOnDirectHit());
    }

    public boolean getFriendlyFire() {
        return getValue().getBoolean(FRIENDLY_FIRE_PATH, defaultConfig != null && defaultConfig.getFriendlyFire());
    }

    public static void init(@NotNull BouncyProjectile bouncyProjectile, @NotNull BouncyProjectileConfig config) {
        bouncyProjectile.setGroundExplosionTimerTicks(Ticks.secondsToTicks(config.getGroundExplosionTimer()));
        bouncyProjectile.setGravity(config.getGravity());
        bouncyProjectile.setRestitutionFactor(config.getRestitutionFactor());
        bouncyProjectile.setFrictionFactor(config.getFrictionFactor());
        bouncyProjectile.setSticky(config.isSticky());
        bouncyProjectile.setTrail(config.getTrail());
        bouncyProjectile.setBounceMarks(config.getBounceMarks());
        bouncyProjectile.setGlowing(config.getGlowing());
        bouncyProjectile.setRadius(config.getRadius());
        bouncyProjectile.setDamageOnDirectHit(config.getDirectHitDamage());
        bouncyProjectile.setConsumeOnDirectHit(config.getConsumeOnDirectHit());
        bouncyProjectile.setFriendlyFire(config.getFriendlyFire());
    }
}
