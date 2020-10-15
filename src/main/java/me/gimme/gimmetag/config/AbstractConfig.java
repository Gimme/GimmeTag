package me.gimme.gimmetag.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractConfig<T> implements IConfig<T> {

    private final IConfig<ConfigurationSection> parent;
    private final String path;

    protected AbstractConfig(@NotNull IConfig<ConfigurationSection> parent, @NotNull String path) {
        this.parent = parent;
        this.path = path;
    }

    @NotNull
    public abstract T getValue();

    protected @NotNull ConfigurationSection getConfigurationSection() {
        return parent.getValue();
    }

    protected String getPath() {
        return path;
    }
}
