package me.gimme.gimmetag.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class ValueConfig<T> extends AbstractConfig<T> {
    ValueConfig(@NotNull AbstractConfig<ConfigurationSection> parent, @NotNull String path, @NotNull Class<? extends T> cls) {
        super(parent, path, cls);
    }

    ValueConfig(@NotNull ConfigurationSection configurationSection, @NotNull String path, @NotNull Class<? extends T> cls) {
        super(configurationSection, path, cls);
    }

    @NotNull
    @Override
    public T getValue() {
        return (T) Objects.requireNonNull(getConfigurationSection().getObject(getPath(), Object.class));
    }
}
