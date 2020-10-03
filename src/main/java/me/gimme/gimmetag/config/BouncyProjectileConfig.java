package me.gimme.gimmetag.config;

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

    @Nullable
    private BouncyProjectileConfig defaultConfig;

    BouncyProjectileConfig(@NotNull AbstractConfig<ConfigurationSection> parent, @NotNull String path,
                           @Nullable BouncyProjectileConfig defaultConfig) {
        super(parent, path);
        this.defaultConfig = defaultConfig;
    }

    BouncyProjectileConfig(@NotNull String path, @Nullable BouncyProjectileConfig defaultConfig) {
        super(path);
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
}
