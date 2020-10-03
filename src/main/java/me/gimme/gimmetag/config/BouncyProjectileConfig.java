package me.gimme.gimmetag.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class BouncyProjectileConfig extends AbilityItemConfig {
    private static final String SPEED_PATH = "speed";
    private static final String GRAVITY_PATH = "gravity";
    private static final String MAX_EXPLOSION_TIMER_PATH = "max-explosion-timer";
    private static final String GROUND_EXPLOSION_TIMER_PATH = "ground-explosion-timer";

    public BouncyProjectileConfig(@NotNull ConfigurationSection parent, @NotNull String path) {
        super(parent, path);
    }

    BouncyProjectileConfig(@NotNull AbstractConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path);
    }

    BouncyProjectileConfig(@NotNull String path) {
        super(path);
    }

    public double getSpeed() {
        return getValue().getDouble(SPEED_PATH);
    }

    public double getGravity() {
        return getValue().getDouble(GRAVITY_PATH);
    }

    public double getMaxExplosionTimer() {
        return getValue().getDouble(MAX_EXPLOSION_TIMER_PATH);
    }

    public double getGroundExplosionTimer() {
        return getValue().getDouble(GROUND_EXPLOSION_TIMER_PATH);
    }
}
