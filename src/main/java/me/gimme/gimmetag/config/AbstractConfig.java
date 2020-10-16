package me.gimme.gimmetag.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractConfig<T> implements IConfig<T> {

    private final IConfig<ConfigurationSection> parent;
    private final String path;

    protected AbstractConfig(@NotNull IConfig<ConfigurationSection> parent, @NotNull String path) {
        this.parent = parent;
        this.path = path;
    }

    @Override
    @Nullable
    public T getValue() {
        return getValue(parent.getValue());
    }

    @Nullable
    public abstract T getValue(@NotNull ConfigurationSection configurationSection);

    public @NotNull IConfig<ConfigurationSection> getParent() {
        return parent;
    }

    public String getPath() {
        return path;
    }
}
