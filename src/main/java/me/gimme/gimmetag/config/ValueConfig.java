package me.gimme.gimmetag.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class ValueConfig<T> extends AbstractConfig<T> {
    ValueConfig(@NotNull AbstractConfig<ConfigurationSection> parent, @NotNull String path, @NotNull Class<? extends T> cls) {
        super(parent, path, cls);
    }

    ValueConfig(@NotNull String path, @NotNull Class<? extends T> cls) {
        super(path, cls);
    }

    @NotNull
    @Override
    public T getValue() {
        // TODO: safe type check?, make sure the config values get loaded on first run?
        return (T) Objects.requireNonNull(getConfig().get(path, cls));
    }
}
