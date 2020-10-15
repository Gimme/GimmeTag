package me.gimme.gimmetag.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConfig<T> {

    private static final List<AbstractConfig<?>> values = new ArrayList<>();

    private final ConfigurationSection configurationSection;
    private final String path;

    AbstractConfig(@NotNull AbstractConfig<ConfigurationSection> parent, @NotNull String path) {
        this(parent.getValue(), path);
    }

    AbstractConfig(@NotNull ConfigurationSection configurationSection, @NotNull String path) {
        this.configurationSection = configurationSection;
        this.path = path;

        values.add(this);
    }

    @NotNull
    public abstract T getValue();

    @NotNull
    protected ConfigurationSection getConfigurationSection() {
        return configurationSection;
    }

    protected String getPath() {
        return path;
    }


    public static AbstractConfig<?>[] values() {
        return values.toArray(new AbstractConfig<?>[0]);
    }
}
