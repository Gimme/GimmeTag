package me.gimme.gimmetag.config.type;

import me.gimme.gimmetag.config.IConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class AbilityItemConfig extends ValueConfig<ConfigurationSection> {

    private static final String COOLDOWN_PATH = "cooldown";
    private static final String RECHARGE_TIME_PATH = "recharge";
    private static final String CONSUMABLE_PATH = "consumable";
    private static final String DURATION_PATH = "duration";
    private static final String LEVEL_PATH = "level";

    public AbilityItemConfig(@NotNull IConfig<ConfigurationSection> parent, @NotNull String path) {
        super(parent, path);
    }

    public AbilityItemConfig(@NotNull ConfigurationSection section) {
        this(section::getParent, section.getName());
    }

    public double getCooldown() {
        return getValue().getDouble(COOLDOWN_PATH, 0);
    }

    public double getRechargeTime() {
        return getValue().getDouble(RECHARGE_TIME_PATH, 0);
    }

    public boolean isConsumable() {
        return getValue().getBoolean(CONSUMABLE_PATH, false);
    }

    public double getDuration() {
        return getValue().getDouble(DURATION_PATH, 0);
    }

    public int getLevel() {
        return getValue().getInt(LEVEL_PATH, 0);
    }
}
